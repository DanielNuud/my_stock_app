package daniel.nuud.currencyservice.service;

import daniel.nuud.currencyservice.dto.RateResponse;
import daniel.nuud.currencyservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final WebClient webClient;

    @Value("${freecurrency.api.key}")
    private String apiKey;

    @Cacheable(cacheNames = "currency", key = "#p0")
    public Map<String, String> getCurrencyRates(String currency) {
        RateResponse response = webClient.get()
                .uri("/v1/latest?apikey={apikey}&base_currency={base_currency}", apiKey, currency)
                .retrieve()
                .bodyToMono(RateResponse.class)
                .block();

        if (response == null || response.getRates() == null) {
            throw new ResourceNotFoundException("Currency \"" + currency + "\" or currency rates not found");
        }

       return response.getRates();
    }

    public Double convert(String fromCurrency, String toCurrency, Double amount) {
        Map<String, String> rates = getCurrencyRates(fromCurrency);
        Double rateTo = Double.parseDouble(rates.get(toCurrency));
        return amount * rateTo;
    }
}
