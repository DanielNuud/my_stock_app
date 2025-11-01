package daniel.nuud.stocksservice.subscriptions;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class Subscription {

    private final ConcurrentHashMap<String, Set<String>> subs = new ConcurrentHashMap<>();

    public boolean subscribe(String userKey, String ticker) {
        String t = ticker.toUpperCase();
        subs.compute(t, (key, oldSet) -> {
            if (oldSet == null) {
                Set<String> s = ConcurrentHashMap.newKeySet();
                s.add(userKey);
                return s;
            } else {
                oldSet.add(userKey);
                return oldSet;
            }
        });
        return subs.get(t).size() == 1;
    }

    public boolean unsubscribe(String userKey, String ticker) {
        String t = ticker.toUpperCase();
        return subs.computeIfPresent(t, (key, set) -> {
            set.remove(userKey);
            return set.isEmpty() ? null : set;
        }) == null;
    }

//    public Set<String> listeners(String ticker) {
//        return tickerToUsers.getOrDefault(
//                ticker.toUpperCase(),
//                Set.of()
//        );
//    }
}
