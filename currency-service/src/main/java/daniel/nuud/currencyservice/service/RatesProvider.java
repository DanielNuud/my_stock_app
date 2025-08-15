package daniel.nuud.currencyservice.service;

import daniel.nuud.currencyservice.dto.RateResponse;
import daniel.nuud.currencyservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RatesProvider {

    private final FreeCurrencyClient freeCurrencyClient;

    @Value("${freecurrency.api.key}")
    private String apiKey;

    @Cacheable(cacheNames = "currency",
            key = "#base.toUpperCase()",
            unless = "#result == null || #result.isEmpty()")
    public Map<String, String> getRates(String base) {

        RateResponse resp = freeCurrencyClient.getCurrencyRates(base.toUpperCase().trim(), apiKey);

        var rates = (resp == null) ? null : resp.getRates();

        if (rates == null || rates.isEmpty()) {
            throw new ResourceNotFoundException("Currency \"" + base + "\" or currency rates not found");
        }

        return rates;
    }
}
