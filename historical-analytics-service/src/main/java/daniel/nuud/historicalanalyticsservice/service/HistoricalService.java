package daniel.nuud.historicalanalyticsservice.service;

import daniel.nuud.historicalanalyticsservice.dto.ApiResponse;
import daniel.nuud.historicalanalyticsservice.exception.ResourceNotFoundException;
import daniel.nuud.historicalanalyticsservice.model.StockBar;
import daniel.nuud.historicalanalyticsservice.model.Timespan;
import daniel.nuud.historicalanalyticsservice.repository.StockBarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Slf4j
@Service
public class HistoricalService {

    private final WebClient webClient;

    private final StockBarRepository stockBarRepository;

    @Value("${polygon.api.key}")
    private String apiKey;

    public HistoricalService(WebClient webClient, StockBarRepository stockBarRepository) {
        this.webClient = webClient;
        this.stockBarRepository = stockBarRepository;
    }

    public StockBar fetchStockBar(String ticker, LocalDate from, LocalDate to, Integer multiplier, String timespan) {
      log.info("Fetching stock bar for ticker {}", ticker);

        ApiResponse response = webClient.get()
                .uri("/v2/aggs/ticker/{ticker}/range/{multiplier}/{timespan}/{from}/{to}?adjusted=true&sort=asc&apiKey={apiKey}",
                        ticker, multiplier, timespan, from, to, apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();

        if (response.getResults() == null || response == null) {
            throw new ResourceNotFoundException("Data with ticker " + ticker + " not found");
        }

        StockBar stockBar = new StockBar();
        stockBar.setTicker(ticker);
        stockBar.setClosePrice(response.getResults().getClosePrice());
        stockBar.setOpenPrice(response.getResults().getOpenPrice());
        stockBar.setHighPrice(response.getResults().getHighPrice());
        stockBar.setLowPrice(response.getResults().getLowPrice());
        stockBar.setVolume(response.getResults().getVolume());
        stockBar.setNumberOfTransactions(response.getResults().getNumberOfTransactions());
        stockBar.setTimestamp(response.getResults().getTimestamp());

        stockBarRepository.save(stockBar);

        return stockBar;
    }
}
