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
import org.springframework.stereotype.Component;

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
        subscriptions.addTicker(ticker);
        if (webSocket != null) {
            subscriptions.flush(webSocket);
        }
    }

    private void open() {
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

    private String shorten(String s) {
        return (s == null || s.length() <= 200) ? s : s.substring(0, 200) + "...";
    }
}
