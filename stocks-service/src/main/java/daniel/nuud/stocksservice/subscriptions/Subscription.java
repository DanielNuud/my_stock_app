package daniel.nuud.stocksservice.subscriptions;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class Subscription {

    private final ConcurrentMap<String, Set<String>> tickerToUsers =
            new ConcurrentHashMap<>();

    public void subscribe(String userKey, String ticker) {
        tickerToUsers
                .computeIfAbsent(
                        ticker.toUpperCase(),
                        k -> ConcurrentHashMap.newKeySet()
                )
                .add(userKey);
    }

    public void unsubscribe(String userKey, String ticker) {
        tickerToUsers.computeIfPresent(
                ticker.toUpperCase(),
                (k, set) -> {
                    set.remove(userKey);
                    return set.isEmpty() ? null : set;
                }
        );
    }

    public Set<String> listeners(String ticker) {
        return tickerToUsers.getOrDefault(
                ticker.toUpperCase(),
                Set.of()
        );
    }
}
