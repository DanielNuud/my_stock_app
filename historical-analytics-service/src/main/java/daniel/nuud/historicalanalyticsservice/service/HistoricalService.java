package daniel.nuud.historicalanalyticsservice.service;

import daniel.nuud.historicalanalyticsservice.dto.ApiResponse;
import daniel.nuud.historicalanalyticsservice.dto.StockBarApi;
import daniel.nuud.historicalanalyticsservice.model.Period;
import daniel.nuud.historicalanalyticsservice.model.StockBar;
import daniel.nuud.historicalanalyticsservice.model.StockBarId;
import daniel.nuud.historicalanalyticsservice.repository.StockBarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

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

    private LocalDate toNow = LocalDate.now();

//    public List<StockBar> fetchStockBar(String ticker, LocalDate from, LocalDate to, Integer multiplier, String timespan) {
//      log.info("Fetching stock bar for ticker {}", ticker);
//
//        ApiResponse response = webClient.get()
//                .uri("/v2/aggs/ticker/{ticker}/range/{multiplier}/{timespan}/{from}/{to}?adjusted=true&sort=asc&apiKey={apiKey}",
//                        ticker, multiplier, timespan, from, to, apiKey)
//                .retrieve()
//                .bodyToMono(ApiResponse.class)
//                .block();
//
//        if (response.getResults() == null || response == null) {
//            throw new ResourceNotFoundException("Data with ticker " + ticker + " not found");
//        }
//
//        List<StockBar> stockBars = response.getResults().stream()
//                .map(dto -> mapToEntity(ticker, dto))
//                        .toList();
//
//        return stockBarRepository.saveAll(stockBars);
//    }

    private StockBar mapToEntity(String ticker, StockBarApi dto) {
        LocalDate date = Instant.ofEpochMilli(dto.getTimestamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        StockBarId id = new StockBarId(ticker, date);

        return new StockBar(
                id,
                dto.getClosePrice(),
                dto.getLowPrice(),
                dto.getHighPrice(),
                dto.getOpenPrice(),
                dto.getVolume(),
                dto.getNumberOfTransactions(),
                dto.getTimestamp()
        );
    }

    public List<StockBar> getHistoricalStockBar(String ticker, String period, Integer multiplier, String timespan) {
        Period fromStringToPeriod = Period.valueOf(period.toUpperCase());
        LocalDate fromDate = determinePeriod(fromStringToPeriod);

        List<StockBar> existingBars = stockBarRepository.findByTickerAndDateRange(ticker, fromDate, toNow);

        long expectedCount = ChronoUnit.DAYS.between(fromDate, toNow) + 1;
        if (existingBars.size() == expectedCount) {
            return existingBars.stream()
                    .sorted(Comparator.comparing(sb -> sb.getId().getDate()))
                    .toList();
        }

        log.info("Fetching stock bar for ticker {}", ticker);
        ApiResponse response = webClient.get()
                .uri("/v2/aggs/ticker/{ticker}/range/{multiplier}/{timespan}/{from}/{to}?adjusted=true&sort=asc&apiKey={apiKey}",
                        ticker, multiplier, timespan, fromDate, toNow, apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();

        if (response != null && response.getResults() != null) {
            List<StockBar> stockBars = response.getResults().stream()
                    .map(dto -> mapToEntity(ticker, dto))
                    .toList();

            stockBarRepository.saveAll(stockBars);
        }

        return stockBarRepository.findByTickerAndDateRange(ticker, fromDate, toNow).stream()
                .sorted(Comparator.comparing(sb -> sb.getId().getDate()))
                .toList();
    }

    private LocalDate determinePeriod(Period from) {

        LocalDate toDate = LocalDate.now();
        return switch (from) {
            case ONE_DAY -> toDate.minusDays(1);
            case ONE_WEEK -> toDate.minusWeeks(1);
            case ONE_MONTH -> toDate.minusMonths(1);
            case ONE_YEAR -> toDate.minusYears(1);
            case FIVE_YEARS -> toDate.minusYears(5);
        };
    }
}
