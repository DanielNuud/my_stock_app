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
            Arrays.asList("AAPL", "MSFT", "TSLA", "NVDA", "GOOGL", "AMZN", "AMD", "INTC", "META", "NFLX");

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

                    )
                    .exec(
                            http("GET historical one year")
                                    .get("/api/historical/#{ticker}")
                                    .queryParam("period", "one_year")
                                    .check(status().is(200))

                    );

    private ChainBuilder wsSubscribeAwaitConvertAndCleanup() {
        return
                exec(ws("WS open").connect("/ws/stocks"))
                        .exec(
                                http("POST subscribe")
                                        .post("/api/stocks/subscribe/#{ticker}")
                                        .check(status().in(200, 201, 204))
                        )
                        .pause(Duration.ofMillis(200))
                        .tryMax(1).on(
                                ws("WS await first tick")
                                        .sendText("ping")
                                        .await(Duration.ofSeconds(10))
                                        .on(polygonAggMinuteCheck)
                        ).exitHereIfFailed()
                        .repeat(3).on(
                                exec(
                                        http("GET currency convert")
                                                .get("/api/currency/convert")
                                                .queryParam("from", "USD")
                                                .queryParam("to", "EUR")
                                                .queryParam("amount", "100")
                                                .check(status().is(200))
                                ).pause(Duration.ofMillis(500))
                        )
                        .exec(
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
                        )
                        .exec(
                                http("DELETE unsubscribe")
                                        .delete("/api/stocks/subscribe/#{ticker}")
                                        .check(status().in(200, 204))
                        )
                        .exec(ws("WS close").close());
    }

    WsFrameCheck polygonAggMinuteCheck =
            ws.checkTextMessage("polygon-agg-minute")
                    .matching(regex("\"sym\"\\s*:\\s*\"#{ticker}\""))
                    .check(jmesPath("[0].ev").is("AM"))
                    .check(jmesPath("[0].sym").is("#{ticker}"))
                    .check(jmesPath("[0].c").ofDouble().gt(0.0).saveAs("lastClose"))
                    .check(jmesPath("[0].s").ofLong().saveAs("windowStartMs"))
                    .check(jmesPath("[0].e").ofLong().saveAs("windowEndMs"));

    ScenarioBuilder scn = scenario("Master user journey")
            .exec(session -> session.set("userKey", "guest").set("period", "one_week"))
            .exec(chooseTickerOnce)
            .group("Search")
            .on(typeAheadSearch)
            .group("CompanyPage")
            .on(companyPage)
            .group("WebSocket")
            .on(wsSubscribeAwaitConvertAndCleanup()
            );

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
