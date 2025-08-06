package daniel.nuud.stocksservice.service;

import daniel.nuud.stocksservice.dto.StockPriceDto;
import daniel.nuud.stocksservice.model.StockPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Slf4j
@RequiredArgsConstructor
public class StocksPriceService {

    private final SimpMessagingTemplate messagingTemplate;

    private final RestClient restClient;

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
        sendStockBar(stockPrice);

        log.info("Broadcasting via WebSocket: {}", stockPrice);
        messagingTemplate.convertAndSend("/topic/stocks/" + ticker, stockPrice);
    }

    public void sendStockBar(StockPrice stockPrice) {
        log.info("Sending stock bar to historical: {}", stockPrice);
        restClient.post()
                .uri("http://historical-service:8080/api/historical/realtime")
                .body(stockPrice)
                .retrieve()
                .toBodilessEntity();
    }


    public List<StockPrice> getPrices(String ticker) {
        return new ArrayList<>(priceMap.getOrDefault(ticker, new LinkedList<>()));
    }

    public Set<String> getTickers() {
        return priceMap.keySet();
    }
}
