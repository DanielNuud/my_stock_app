package daniel.nuud.currencyservice.service;

import daniel.nuud.currencyservice.dto.RateResponse;
import daniel.nuud.currencyservice.exception.ResourceNotFoundException;
import daniel.nuud.currencyservice.notification.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final RestClient restClient;
    private final NotificationClient notifications;

    @Value("${freecurrency.api.key}")
    private String apiKey;

    @Cacheable(cacheNames = "currency", key = "#p0")
    public Map<String, String> getCurrencyRates(String currency) {
        RateResponse response = restClient.get()
                .uri("/v1/latest?apikey={apikey}&base_currency={base_currency}", apiKey, currency)
                .retrieve()
                .body(RateResponse.class);

        if (response == null || response.getRates() == null) {
            throw new ResourceNotFoundException("Currency \"" + currency + "\" or currency rates not found");
        }

       return response.getRates();
    }

    public Double convert(String from, String to, Double amount, String userKey) {
        Map<String, String> rates = getCurrencyRates(from);
        Double rateTo = Double.parseDouble(rates.get(to));
        Double result = amount * rateTo;

        try {
            notifications.sendNotification(
                    userKey,
                    "Conversion completed",
                    amount + " " + from + " → " + String.format("%.4f", result) + " " + to,
                    "INFO",
                    "FX:CONVERT:" + from + ":" + to + ":" + amount
            );
        } catch (Exception e) {
            notifications.sendNotification(
                    userKey,
                    "Conversion failed",
                    "Please check your currency rates and try again.",
                    "ERROR",
                    "FX:INVALID:" + epochMinute()
            );
            log.error("Notification convert failed: {}", e.getMessage(), e);
        }

        return result;
    }

    private Long epochMinute() {
        return Instant.now().getEpochSecond() / 60;
    }
}
