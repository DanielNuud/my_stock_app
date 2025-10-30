package daniel.nuud.stocksservice.service;

import daniel.nuud.stocksservice.service.components.ExponentialBackoff;
import daniel.nuud.stocksservice.service.components.PolygonMessageProcessor;
import daniel.nuud.stocksservice.service.components.WsSubscriptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketClient {

    @Value("${polygon.api.key}")
    private String apiKey;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private WebSocket webSocket;

    private final PolygonMessageProcessor messageProcessor;
    private final WsSubscriptions subscriptions;
    private final ExponentialBackoff backoff;

    private final Set<String> activeTickers =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Value("${stocks.ws.mock:false}")
    private boolean mockMode;

    private final Random rnd = new Random();
    private final Map<String, Double> last = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ws-reconnect");
                t.setDaemon(true);
                return t;
            });

    @PostConstruct
    public void start() {
        open();
    }

    @PreDestroy
    public void stop() {
        try {
            if (webSocket != null) {
                webSocket.close(1000, "shutdown");
            }
        } finally {
            scheduler.shutdownNow();
        }
    }

    public void subscribeTo(String ticker) {
        String t = ticker.toUpperCase();

        if (mockMode) {
            activeTickers.add(t);
            return;
        }

        subscriptions.addTicker(ticker);
        if (webSocket != null) {
            subscriptions.flush(webSocket);
        }
    }

    public void unsubscribeFrom(String ticker) {
        String t = ticker.toUpperCase();

        if (mockMode) {
            activeTickers.remove(t);
            return;
        }
    }

    private void open() {
        if (mockMode) {
            log.info("WS in MOCK mode: skip real provider connect");
            return;
        }

        Request request = new Request.Builder()
                .url("wss://delayed.polygon.io/stocks")
                .build();

        log.info("Opening Polygon WS connection...");
        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                log.info("WS connected, sending auth");
                ws.send("{\"action\":\"auth\",\"params\":\"" + apiKey + "\"}");
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                if (maybeHandleStatus(text, ws)) {
                    return;
                }
                try {
                    messageProcessor.process(text);
                    log.info("Message processed: " + text);
                } catch (Exception e) {
                    log.warn("Failed to process message: {}", shorten(text), e);
                }
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                log.warn("WS closed: {} - {}", code, reason);
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                log.error("WS failure: {}", t.toString());
                scheduleReconnect();
            }
        });
    }

    private boolean maybeHandleStatus(String text, WebSocket ws) {
        try {
            JSONArray arr = new JSONArray(text);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject json = arr.getJSONObject(i);
                if (!"status".equals(json.optString("ev"))) continue;

                String status = json.optString("status");
                String message = json.optString("message");
                log.info("WS status: {} - {}", status, message);

                if ("authenticated".equalsIgnoreCase(status)) {
                    backoff.reset();
                    subscriptions.flush(ws);
                }
                return true;
            }
        } catch (Exception ignore) {

        }
        return false;
    }

    private void scheduleReconnect() {
        long delay = backoff.nextDelayMs();
        log.info("Scheduling reconnect in {} ms", delay);
        scheduler.schedule(this::open, delay, TimeUnit.MILLISECONDS);
    }

    @Scheduled(fixedDelayString = "${mock.ws.period-ms:1000}")
    public void mockTick() {
        if (!mockMode || activeTickers.isEmpty()) return;

        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder(256).append('[');
        boolean first = true;
        for (String t : activeTickers) {
            double prev = last.getOrDefault(t, 120 + rnd.nextDouble() * 80);
            double next = Math.max(1.0, prev + (rnd.nextDouble() - 0.5) * 1.8);
            last.put(t, next);

            if (!first) sb.append(',');
            first = false;
            sb.append('{')
                    .append("\"ev\":\"AM\",")
                    .append("\"sym\":\"").append(t).append("\",")
                    .append("\"c\":").append(String.format(java.util.Locale.US, "%.2f", next)).append(',')
                    .append("\"s\":").append(now - 60_000).append(',')
                    .append("\"e\":").append(now)
                    .append('}');
        }
        sb.append(']');

        String json = sb.toString();
        try {
            messageProcessor.process(json);
        } catch (Exception e) {
            log.warn("MOCK tick process failed: {}", e.toString());
        }
    }

    private String shorten(String s) {
        return (s == null || s.length() <= 200) ? s : s.substring(0, 200) + "...";
    }
}
