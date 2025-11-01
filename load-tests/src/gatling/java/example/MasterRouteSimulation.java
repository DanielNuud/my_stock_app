package example;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MasterRouteSimulation extends Simulation {

    private static final String BASE_URL   = System.getenv().getOrDefault("BASE_URL", "http://api-gateway:8080");
    private static final String WS_BASE_URL= System.getenv().getOrDefault("WS_BASE_URL","ws://api-gateway:8080");

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .wsBaseUrl(WS_BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling-MasterRoute/Java");

    private static final List<String> TICKERS =
            Arrays.asList("AAPL", "TSLA", "NVDA", "AMZN", "AMD");

    private ChainBuilder chooseTickerOnce =
            exec(session -> {
                String t = TICKERS.get(ThreadLocalRandom.current().nextInt(TICKERS.size()));
                return session.set("ticker", t);
            });

    private ChainBuilder typeAheadSearch =
            repeat(session -> session.getString("ticker").length(), "i").on(
                    exec(session -> {
                        String t = session.getString("ticker");
                        int i = session.getInt("i") + 1;
                        return session.set("q", t.substring(0, i));
                    })
                            .exec(
                                    http("GET tickers search (#{q})")
                                            .get("/api/tickers/search")
                                            .queryParam("query", "#{q}")
                                            .check(status().is(200))
                                            .check(jsonPath("$[0].symbol").optional())
                            )
            );

    private ChainBuilder companyPage =
            exec(
                    http("GET company")
                            .get("/api/companies/#{ticker}")
                            .check(status().is(200))
                            .resources(
                                    http("GET historical one week")
                                            .get("/api/historical/#{ticker}")
                                            .queryParam("period", "one_week")
                                            .check(status().is(200)),
                                    http("GET news")
                                            .get("/api/news/#{ticker}")
                                            .check(status().is(200))
                            )
            )
                    .exec(
                            http("GET historical one month")
                                    .get("/api/historical/#{ticker}")
                                    .queryParam("period", "one_month")
                                    .check(status().is(200))

                    );

    private ChainBuilder notificationsFlow() {
        return exec(
                http("GET notifications")
                        .get("/api/notifications")
                        .queryParam("userKey", "#{userKey}")
                        .check(status().is(200))
                        .check(jmesPath("[0].id").optional().saveAs("notifId"))
        )
                .doIf(session -> session.contains("notifId")).then(
                        exec(
                                http("PATCH notification read")
                                        .patch("/api/notifications/#{notifId}/read")
                                        .check(status().is(204))
                        )
                );
    }

    private ChainBuilder currencyConvertUsdEurDefault() {
        return currencyConvert("USD", "EUR", "100", 3, java.time.Duration.ofMillis(500));
    }

    private ChainBuilder currencyConvert(String from, String to, String amount, int times, java.time.Duration pause) {
        return repeat(times).on(
                exec(
                        http("GET currency convert")
                                .get("/api/currency/convert")
                                .queryParam("from", from)
                                .queryParam("to", to)
                                .queryParam("amount", amount)
                                .check(status().is(200))
                ).pause(pause)
        );
    }

    private static final String STOMP_CONNECT =
            "CONNECT\n" +
                    "accept-version:1.2\n" +
                    "host:stock-app\n" +
                    "heart-beat:0,0\n" +
                    "\n\u0000";

    private static String stompSubscribeFrame(String subId, String ticker) {
        return "SUBSCRIBE\n" +
                "id:" + subId + "\n" +
                "destination:/topic/stocks/" + ticker + "\n" +
                "ack:auto\n" +
                "\n\u0000";
    }

    private ChainBuilder wsSubscribeAwaitAndClose =
                    exec(
                    ws("WS open")
                            .connect(WS_BASE_URL + "/ws/stocks?userKey=#{userKey}")
                            .header("Origin", BASE_URL)
                            .header("Cookie", "userKey=#{userKey}")
                    )
                    .exec(
                            ws("STOMP CONNECT")
                                    .sendText(STOMP_CONNECT)
                                    .await(10)
                                    .on(
                                            ws.checkTextMessage("CONNECTED")
                                                    .matching(regex("(?s)^CONNECTED\\b.*"))
                                    )
                    )
                            .exec(
                                    http("POST subscribe (REST)")
                                            .post("/api/stocks/subscribe/#{ticker}")
                                            .header("X-User-Key", "#{userKey}")
                                            .check(status().in(200, 201, 204))
                            )

                            .exec(session -> session.set("subId", java.util.UUID.randomUUID().toString()))
                    .exec(
                            ws("STOMP SUBSCRIBE /topic/stocks/#{ticker}")
                                    .sendText(session -> stompSubscribeFrame(session.getString("subId"), session.getString("ticker")))
                                    .await(15)
                                    .on(
                                            ws.checkTextMessage("MESSAGE")
                                                    .matching(regex("(?s)^MESSAGE\\b.*destination:/topic/stocks/#{ticker}.*"))
                                                    .check(regex("(?s)\\n\\n(\\{.*?})\\u0000?").saveAs("stompBody"))
                                    )
                    )

                    .exec(session -> {
                        String body = session.getString("stompBody");
                        String tkr  = session.getString("ticker");
                        if (body != null) {
                            java.util.regex.Matcher m =
                                    java.util.regex.Pattern
                                            .compile("\"ticker\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(tkr) + "\".*?\"price\"\\s*:\\s*([0-9.]+)")
                                            .matcher(body);
                            if (m.find()) {
                                session = session.set("lastClose", m.group(1));
                            } else {
//                                System.out.println("WARN: body doesn't contain expected ticker/price: " + body);
                            }
//                            System.out.println("STOMP BODY for " + tkr + " => " + body);
                        } else {
//                            System.out.println("WARN: no STOMP body received");
                        }
                        return session;
                    })
                    .exec(
                            http("DELETE unsubscribe")
                                    .delete("/api/stocks/subscribe/#{ticker}")
                                    .check(status().in(200, 204))
                    )

                    .exec(
                            ws("STOMP UNSUBSCRIBE /topic/stocks/#{ticker}")
                                    .sendText(session ->
                                            "UNSUBSCRIBE\n" +
                                                    "id:" + session.getString("subId") + "\n\n\u0000"
                                    )
                    )

                    .exec(
                            ws("STOMP DISCONNECT")
                                    .sendText("DISCONNECT\n\n\u0000")
                    )
                    .exec(ws("WS close").close());

    int WS_PCT = Integer.parseInt(System.getProperty("WS_PCT",
            System.getenv().getOrDefault("WS_PCT", "20")));

    private ChainBuilder maybeDoWs =
            exec(session -> {
                boolean pick = ThreadLocalRandom.current().nextInt(100) < WS_PCT;
                return session.set("wsPick", pick);
            })
                    .doIf(session -> session.getBoolean("wsPick")).then(
                            group("WebSocket").on(wsSubscribeAwaitAndClose)
                    );

    ScenarioBuilder scn = scenario("Master user journey")
            .exec(session -> session.set("userKey", "user-" + session.userId()).set("period", "one_week"))
            .exec(chooseTickerOnce)

            .group("Search").on(typeAheadSearch)
            .pause(Duration.ofMillis(800))

            .group("CompanyPage").on(companyPage)
            .pause(Duration.ofMillis(300), Duration.ofMillis(800))

            .group("Currency").on(currencyConvertUsdEurDefault())
            .pause(Duration.ofMillis(200), Duration.ofMillis(500))

            .group("Notifications").on(notificationsFlow())
            .pause(Duration.ofMillis(200), Duration.ofMillis(500))

            .group("WebSocket").on(maybeDoWs)
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(3));
    {
        int START_CONC   = Integer.parseInt(System.getenv().getOrDefault("START_CONC", "10"));
        int TARGET_CONC  = Integer.parseInt(System.getenv().getOrDefault("TARGET_CONC", "300"));
        int RAMP_MIN     = Integer.parseInt(System.getenv().getOrDefault("RAMP_MIN", "5"));
        int HOLD_MIN     = Integer.parseInt(System.getenv().getOrDefault("HOLD_MIN", "20"));
        int RAMPDOWN_SEC = Integer.parseInt(System.getenv().getOrDefault("RAMPDOWN_SEC", "30"));

        setUp(
                scn.injectClosed(
                        rampConcurrentUsers(START_CONC).to(TARGET_CONC).during(Duration.ofMinutes(RAMP_MIN)),
                        constantConcurrentUsers(TARGET_CONC).during(Duration.ofMinutes(HOLD_MIN)),
                        rampConcurrentUsers(TARGET_CONC).to(START_CONC).during(Duration.ofSeconds(RAMPDOWN_SEC))
                )
        )
                .protocols(httpProtocol)
                .maxDuration(
                        Duration.ofMinutes(RAMP_MIN + HOLD_MIN)
                                .plusSeconds(RAMPDOWN_SEC + 60)
                )
                .assertions(
                        global().responseTime().percentile3().lte(3000)
                );
    }
}
