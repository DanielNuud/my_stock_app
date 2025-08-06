package daniel.nuud.historicalanalyticsservice.service;

import daniel.nuud.historicalanalyticsservice.dto.ApiResponse;
import daniel.nuud.historicalanalyticsservice.dto.StockBarApi;
import daniel.nuud.historicalanalyticsservice.dto.StockPrice;
import daniel.nuud.historicalanalyticsservice.model.*;
import daniel.nuud.historicalanalyticsservice.model.Period;
import daniel.nuud.historicalanalyticsservice.repository.StockBarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricalService {

    private final RestClient restClient;

    private final StockBarRepository stockBarRepository;

    @Value("${polygon.api.key}")
    private String apiKey;

    private final LocalDateTime toNow = LocalDateTime.now().with(LocalTime.MAX);

    private final Map<String, StockPrice> latestPrices = new ConcurrentHashMap<>();

    public void saveRealtimePrice(StockPrice stockPrice) {
        log.info("Received real-time price: {}", stockPrice);
        latestPrices.put(stockPrice.getTicker(), stockPrice);
    }

    private StockBar mapToEntity(String ticker, StockBarApi dto) {
        LocalDateTime dateTime = Instant.ofEpochMilli(dto.getTimestamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        StockBarId id = new StockBarId(ticker, dateTime);

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

    public List<StockBar> getHistoricalStockBar(String ticker, String period) {

        Period fromStringToPeriod = Period.valueOf(period.toUpperCase());
        LocalDateTime fromDate = determinePeriod(fromStringToPeriod).with(LocalTime.MIN);

        TimePreset preset = determinePreset(period);
        String multiplier = preset.multiplier();
        String timespan = preset.timespan();

        log.info("Fetching stock bar for ticker {}", ticker);

        String uri = String.format(
                "/v2/aggs/ticker/%s/range/%s/%s/%s/%s?adjusted=true&sort=asc&limit=1500&apiKey=%s",
                ticker, multiplier, timespan, fromDate.toLocalDate(), toNow.toLocalDate(), apiKey
        );

        log.info("Final URI for stock bar request: {}", uri);
        ApiResponse response = getApiResponse(uri);

        if (response != null && response.getResults() != null) {
            List<StockBar> stockBars = response.getResults().stream()
                    .map(dto -> mapToEntity(ticker, dto))
                    .collect(Collectors.toCollection(ArrayList::new));
            log.info("Response raw: {}", response);
            log.info("Results size: {}", response.getResults().size());

            StockPrice latest = latestPrices.get(ticker);

            if (latest != null) {
                stockBars.add(convertToStockBar(latest));
            }

            return stockBars;
//            stockBarRepository.saveAll(stockBars);
        }

//        return stockBarRepository.findByTickerAndDateRange(ticker, fromDate, toNow);
        return List.of();
    }

    private StockBar convertToStockBar(StockPrice stockPrice) {
        StockBar stockBar = new StockBar();
        LocalDateTime dateTime = Instant.ofEpochMilli(stockPrice.getTimestamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        StockBarId id = new StockBarId(stockPrice.getTicker(), dateTime);
        stockBar.setId(id);
        stockBar.setTimestamp(stockPrice.getTimestamp());
        stockBar.setClosePrice(stockPrice.getPrice());
        stockBar.setLowPrice(stockPrice.getPrice());
        stockBar.setHighPrice(stockPrice.getPrice());
        stockBar.setOpenPrice(stockPrice.getPrice());
        stockBar.setVolume(0);
        stockBar.setNumberOfTransactions(0);

        return stockBar;
    }

    private TimePreset determinePreset(String period) {
        return switch (period.toUpperCase()) {
            case "ONE_WEEK" -> new TimePreset("5", "minute");
            case "ONE_MONTH" -> new TimePreset("1", "day");
            case "ONE_YEAR" -> new TimePreset("1", "week");
            default -> new TimePreset("1", "day"); // fallback
        };
    }

    private ApiResponse getApiResponse(String uri) {
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(ApiResponse.class);
    }

    private long estimateExpectedBars(LocalDateTime from, LocalDateTime to, int multiplier, String timespan) {
        Duration totalDuration = Duration.between(from, to);

        long unitSeconds = switch (timespan.toLowerCase()) {
            case "minute" -> 60L;
            case "hour" -> 3600L;
            case "day" -> 86400L;
            case "week" -> 604800L;
            default -> throw new IllegalArgumentException("Unsupported timespan: " + timespan);
        };

        return totalDuration.getSeconds() / (unitSeconds * multiplier);
    }

    private LocalDateTime determinePeriod(Period from) {

        return switch (from) {
            case TODAY -> toNow;
            case YESTERDAY -> toNow.minusDays(1);
            case ONE_WEEK -> toNow.minusWeeks(1);
            case ONE_MONTH -> toNow.minusMonths(1);
            case ONE_YEAR -> toNow.minusYears(1);
            case FIVE_YEARS -> toNow.minusYears(5);
        };
    }

    private String determineTimespan(Timespan timespan) {
        return switch (timespan) {
            case SECOND -> "SECOND";
            case MINUTE -> "MINUTE";
            case HOUR -> "HOUR";
            case DAY -> "DAY";
            case WEEK -> "WEEK";
            case MONTH -> "MONTH";
            case QUARTER ->  "QUARTER";
            case YEAR -> "YEAR";
        };
    }
}
