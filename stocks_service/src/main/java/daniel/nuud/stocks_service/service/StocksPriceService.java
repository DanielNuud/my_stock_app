package daniel.nuud.stocks_service.service;

import daniel.nuud.stocks_service.model.StockPrice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class StocksPriceService {

    private final Map<String, Deque<StockPrice>> priceMap = new ConcurrentHashMap<>();
    private static final int MAX_ENTRIES = 100;

    public void save(String ticker, double price, long timestamp) {
        priceMap.computeIfAbsent(ticker, t -> new ConcurrentLinkedDeque<>());
        Deque<StockPrice> deque = priceMap.get(ticker);

        if (deque.size() >= MAX_ENTRIES) {
            deque.pollFirst();
        }

        deque.addLast(new StockPrice(ticker, price, timestamp));
    }

    public List<StockPrice> getPrices(String ticker) {
        return new ArrayList<>(priceMap.getOrDefault(ticker, new LinkedList<>()));
    }

    public Set<String> getTickers() {
        return priceMap.keySet();
    }
}
