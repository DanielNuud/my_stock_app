package daniel.nuud.currencyservice.service;

import daniel.nuud.currencyservice.dto.RateResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class FreeCurrencyClient {

    private final RestClient freecurrencyApiRestClient;

    @CircuitBreaker(name = "freeCurrencyCB", fallbackMethod = "fallbackNull")
    @Retry(name = "readSafe")
    public RateResponse getCurrencyRates(String base, String apiKey) {
        return freecurrencyApiRestClient.get()
                .uri("/v1/latest?apikey={apikey}&base_currency={base_currency}", apiKey, base)
                .retrieve()
                .body(RateResponse.class);

    }

    private RateResponse fallbackNull(String base, String apiKey, Throwable ex) {
        log.warn("FreeCurrency fallback for {}: {}", base, ex.toString());
        return null;
    }
}
