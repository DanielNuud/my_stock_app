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

    private final NotificationClient notifications;
    private final RatesProvider ratesProvider;

    public Double convert(String from, String to, Double amount, String userKey) {
        String base = from.toUpperCase();
        String quote = to.toUpperCase();

        if (base.equals(quote)) {
            notifySuccess(userKey, base, quote, amount, amount);
            return amount;
        }

        Map<String, String> rates = ratesProvider.getRates(base);
        String raw = rates.get(quote);

        if (raw == null) {
            throw new ResourceNotFoundException("Rate " + base + "→" + quote + " not found");
        }

        java.math.BigDecimal res = new java.math.BigDecimal(amount.toString())
                .multiply(new java.math.BigDecimal(raw));

        double result = res.doubleValue();
        notifySuccess(userKey, base, quote, amount, result);
        return result;
    }

    private void notifySuccess(String userKey, String from, String to, Double amount, Double result) {
        try {
            notifications.sendNotification(
                    userKey,
                    "Conversion completed",
                    amount + " " + from + " → " + String.format("%.4f", result) + " " + to,
                    "INFO",
                    "FX:CONVERT:" + from + ":" + to + ":" + amount
            );
        } catch (Exception e) {
            try {
                notifications.sendNotification(
                        userKey,
                        "Conversion failed",
                        "Please check your currency rates and try again.",
                        "ERROR",
                        "FX:INVALID:" + epochMinute()
                );
            } catch (Exception ignore) { }
            log.error("Notification convert failed: {}", e.getMessage(), e);
        }
    }

    private Long epochMinute() {
        return Instant.now().getEpochSecond() / 60;
    }
}
