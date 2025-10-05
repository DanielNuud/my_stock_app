package daniel.nuud.stocksservice.service.components;

import okhttp3.WebSocket;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WsSubscriptions {
    private final Set<String> channels = new CopyOnWriteArraySet<>();
    private final Queue<String> pending = new ConcurrentLinkedQueue<>();

    public void addTicker(String ticker) {
        String ch = "A." + ticker.toUpperCase();
        channels.add(ch);
        pending.add(ch);
    }

    public void flush(WebSocket ws) {
        String ch;
        while ((ch = pending.poll()) != null) {
            ws.send("{\"action\":\"subscribe\",\"params\":\"" + ch + "\"}");
        }
        if (!channels.isEmpty()) {
            ws.send("{\"action\":\"subscribe\",\"params\":\"" + String.join(",", channels) + "\"}");
        }
    }
}
