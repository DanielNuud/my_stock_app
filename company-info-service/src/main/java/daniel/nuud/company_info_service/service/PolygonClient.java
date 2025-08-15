package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.api.ApiResponse;
import daniel.nuud.company_info_service.dto.api.TickerApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolygonClient {

    private final RestClient polygonRestClient;

    @CircuitBreaker(name = "polygonCompanyCB", fallbackMethod = "fallbackNull")
    @Retry(name = "readSafe")
    public ApiResponse getApiResponse(String ticker, String apiKey) {
        log.info(">>> fetchCompany called for {}", ticker);
        return polygonRestClient.get()
                .uri("/v3/reference/tickers/{ticker}?apiKey={apiKey}", ticker.toUpperCase(), apiKey)
                .retrieve()
                .body(ApiResponse.class);
    }

    private ApiResponse fallbackNull(String ticker, String apiKey, Throwable ex) {
        log.warn("Polygon fallback for {}: {}", ticker, ex.toString());
        return null;
    }

    @CircuitBreaker(name = "polygonCompanyCB", fallbackMethod = "fallbackNull")
    @Retry(name = "readSafe")
    public TickerApiResponse getTickerApiResponse(String query, String apiKey) {
        return polygonRestClient.get()
                .uri("/v3/reference/tickers?market=stocks&search={query}&apiKey={apiKey}", query.toUpperCase(), apiKey)
                .retrieve()
                .body(TickerApiResponse.class);
    }
}
