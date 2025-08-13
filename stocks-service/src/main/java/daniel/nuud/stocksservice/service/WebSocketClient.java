package daniel.nuud.stocksservice.service;

import daniel.nuud.stocksservice.model.StockPrice;
import daniel.nuud.stocksservice.notification.TenPercentMoveEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketClient {

    @Value("${polygon.api.key}")
    private String API_KEY;
    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;

    private final TenPercentMoveEngine tenPercentMoveEngine;
    private final StocksPriceService stockPriceService;

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

                try {
                    JSONArray array = new JSONArray(text);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = array.getJSONObject(i);

                        String event = json.optString("ev", "");

                        switch (event) {
                            case "status":
                                String status = json.optString("status");
                                String message = json.optString("message");
                                System.out.println("Status: " + status + " | Message: " + message);
                                break;

                            case "AM":
                                String ticker = json.getString("sym");
                                double close = json.getDouble("c");
                                long timestamp = json.getLong("e");

                                System.out.println("Saving stock: " + ticker + " -> " + close + " @ " + timestamp);
                                stockPriceService.save(ticker, close, timestamp);

                                tenPercentMoveEngine.onPrice(
                                        new StockPrice(ticker, close, timestamp)
                                );

                                break;

                            default:
                                System.out.println("Unknown event type: " + event);
                                break;
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Failed to parse or process message: " + text);
                    e.printStackTrace();
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
            String message = "{\"action\":\"subscribe\",\"params\":\"AM." + ticker + "\"}";
            System.out.println("Subscribing to: " + message);
            webSocket.send(message);
        }
    }
}
