package daniel.nuud.stocksservice.service;

import daniel.nuud.stocksservice.model.StockPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class StocksPriceService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, Deque<StockPrice>> priceMap = new ConcurrentHashMap<>();
    private static final int MAX_ENTRIES = 100;

    public void save(String ticker, double price, long timestamp) {
        priceMap.computeIfAbsent(ticker, t -> new ConcurrentLinkedDeque<>());
        Deque<StockPrice> deque = priceMap.get(ticker);

        if (deque.size() >= MAX_ENTRIES) {
            deque.pollFirst();
        }

        StockPrice stockPrice = new StockPrice(ticker, price, timestamp);
        deque.addLast(stockPrice);

        messagingTemplate.convertAndSend("/topic/stocks/" + ticker, stockPrice);
    }

    public List<StockPrice> getPrices(String ticker) {
        return new ArrayList<>(priceMap.getOrDefault(ticker, new LinkedList<>()));
    }

    public Set<String> getTickers() {
        return priceMap.keySet();
    }
}
