package daniel.nuud.stocksservice.service;

import daniel.nuud.stocksservice.dto.StockPriceDto;
import daniel.nuud.stocksservice.model.StockPrice;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final RestClient restClientCurrency;

    private final Map<String, Deque<StockPrice>> priceMap = new ConcurrentHashMap<>();
    private static final int MAX_ENTRIES = 100;

    public void save(String ticker, double price, long timestamp) {
        save(ticker, price, timestamp, null);
    }

    public void save(String ticker, double price, long timestamp, @Nullable String targetCurrency) {
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


        if (targetCurrency != null && !targetCurrency.equals("USD")) {
            broadcastStockBar(stockPrice, targetCurrency);
        }
    }

    public void sendStockBar(StockPrice stockPrice) {
        log.info("Sending stock bar to historical: {}", stockPrice);
        restClient.post()
                .uri("http://historical-service:8080/api/historical/realtime")
                .body(stockPrice)
                .retrieve()
                .toBodilessEntity();
    }

    public void broadcastStockBar(StockPrice stockPrice, String targetCurrency) {
        Double amount = stockPrice.getPrice();
        Double convertedPrice = restClientCurrency.get()
                .uri("http://currency-service:8080/api/currency/convert?from=USD&to={targetCurrency}&amount={amount}",
                        targetCurrency, amount)
                .retrieve()
                .body(Double.class);

        StockPriceDto payload = new StockPriceDto(convertedPrice, stockPrice.getTimestamp(),  stockPrice.getTicker());

        messagingTemplate.convertAndSend("/topic/stocks/" + stockPrice.getTicker() + "/" + targetCurrency, payload);
    }


    public List<StockPrice> getPrices(String ticker) {
        return new ArrayList<>(priceMap.getOrDefault(ticker, new LinkedList<>()));
    }

    public Set<String> getTickers() {
        return priceMap.keySet();
    }
}
