package daniel.nuud.historicalanalyticsservice.service;

import daniel.nuud.historicalanalyticsservice.dto.ApiResponse;
import daniel.nuud.historicalanalyticsservice.dto.StockBarApi;
import daniel.nuud.historicalanalyticsservice.dto.StockPrice;
import daniel.nuud.historicalanalyticsservice.model.*;
import daniel.nuud.historicalanalyticsservice.model.Period;
import daniel.nuud.historicalanalyticsservice.notification.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class  HistoricalService {

    private final PolygonClient polygonClient;

    private final NotificationClient notificationClient;

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

    public List<StockBar> getHistoricalStockBar(String ticker, String period, String userKey) {
        Period p = Period.valueOf(period.toUpperCase());
        LocalDateTime fromDate = determinePeriod(p).with(LocalTime.MIN);

        TimePreset preset = determinePreset(period);
        String multiplier = preset.multiplier();
        String timespan   = preset.timespan();

        try {
            log.info("Fetching stock bar for ticker {}", ticker);

            String uri = buildAggsUri(ticker, multiplier, timespan, fromDate);
            log.info("Final URI for stock bar request: {}", uri);
            ApiResponse response = polygonClient.getApiResponse(uri);

            if (response != null && response.getResults() != null) {
                List<StockBar> stockBars = toBars(response, ticker);
                notifyReady(userKey, ticker, period);
                return stockBars;
            } else {
                notifyFailed(userKey, ticker, period, null);
            }
        } catch (Exception e) {
            notifyFailed(userKey, ticker, period, e);
            log.error("Error while fetching stock bar", e);
        }
        return List.of();
    }

    private List<StockBar> toBars(ApiResponse response, String ticker) {
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
    }

    private String buildAggsUri(String ticker, String multiplier, String span, LocalDateTime fromDate) {
        return String.format(
                "/v2/aggs/ticker/%s/range/%s/%s/%s/%s?adjusted=true&sort=asc&limit=1500&apiKey=%s",
                ticker, multiplier, span, fromDate.toLocalDate(), toNow.toLocalDate(), apiKey
        );
    }

    private void notifyReady(String userKey, String ticker, String period) {
        notificationClient.sendNotification(
                userKey,
                "Chart ready",
                ticker.toUpperCase() + " (" + period + ") is ready",
                "INFO",
                "CHART:READY:" + ticker.toUpperCase() + ":" + period
        );
    }

    private void notifyFailed(String userKey, String ticker, String period, Exception cause) {
        try {
            notificationClient.sendNotification(
                    userKey,
                    "Chart fetch failed",
                    "Please try again later.",
                    "ERROR",
                    "CHART:FAILED:" + ticker.toUpperCase() + ":" + period.toUpperCase()
            );
        } catch (Exception ex) {
            log.warn("Failed to send failure notification for {} ({}): {}", ticker, period, ex.toString());
        }
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
}
