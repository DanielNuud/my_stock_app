package daniel.nuud.stocksservice.notification;

import daniel.nuud.stocksservice.model.StockPrice;
import daniel.nuud.stocksservice.subscriptions.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class TenPercentMoveEngine {

    private static final double THRESHOLD = 0.10;
    private final NotificationClient notifications;
    private final Subscription subscriptions;

    private final ConcurrentMap<String, Double> anchor = new ConcurrentHashMap<>();

    public void onPrice(StockPrice p) {
        String ticker = p.getTicker().toUpperCase();
        double curr = p.getPrice();
        if (!Double.isFinite(curr) || curr <= 0) return;

        if (subscriptions.listeners(ticker).isEmpty()) return;

        Double base = anchor.get(ticker);
        if (base == null) {
            anchor.put(ticker, curr);
            return;
        }

        double change = (curr - base) / base;
        if (Math.abs(change) >= THRESHOLD) {
            String dir = change > 0 ? "UP" : "DOWN";
            String pct = String.format("%.1f", Math.abs(change) * 100.0);
            long approx = Math.round(base);


            for (String userKey : subscriptions.listeners(ticker)) {
                notifications.sendNotification(
                        userKey,
                        "Price move " + dir,
                        ticker + " moved " + pct + "% from ~" + approx,
                        "WARN",
                        "STOCKS:MOVE10:" + ticker + ":" + dir
                );
            }

            anchor.put(ticker, curr);
        }
    }
}
