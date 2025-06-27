package daniel.nuud.stocksservice.service;

import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebSocketClient {

    @Value("${polygon.api.key}")
    private String API_KEY;
    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;

    @Autowired
    private StocksPriceService stockPriceService;

    @PostConstruct
    public void connect() {
        Request request = new Request.Builder()
                .url("wss://delayed.polygon.io/stocks")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("WebSocket connected");
                webSocket.send("{\"action\":\"auth\",\"params\":\"" + API_KEY + "\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                System.out.println("Message: " + text);

                if (text.contains("\"status\":\"auth_success\"")) {
                    System.out.println("Authenticated, ready for subscriptions.");
                }

                if (text.contains("\"ev\":\"A\"")) {
                    try {
                        String ticker = text.split("\"sym\":\"")[1].split("\"")[0];
                        double close = Double.parseDouble(text.split("\"c\":")[1].split(",")[0]);
                        long timestamp = Long.parseLong(text.split("\"e\":")[1].split("}")[0]);

                        stockPriceService.save(ticker, close, timestamp);
                    } catch (Exception e) {
                        System.err.println("Failed to parse aggregate message: " + text);
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("WebSocket error: " + t.getMessage());
            }
        });
    }

    public void subscribeTo(String ticker) {
        if (webSocket != null) {
            String message = "{\"action\":\"subscribe\",\"params\":\"A." + ticker + "\"}";
            System.out.println("Subscribing to: " + message);
            webSocket.send(message);
        }
    }
}
