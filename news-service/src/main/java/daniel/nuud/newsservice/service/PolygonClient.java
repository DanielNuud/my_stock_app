package daniel.nuud.newsservice.service;

import daniel.nuud.newsservice.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PolygonClient {

    private final RestClient polygonRestClient;

    @CircuitBreaker(name = "polygonNewsCB", fallbackMethod = "fallbackEmpty")
    @Retry(name = "readSafe")
    public ApiResponse getApiResponse(String ticker, String apiKey) {
        return polygonRestClient.get()
                .uri("/v2/reference/news?ticker={ticker}&order=asc&limit=10&sort=published_utc&apiKey={apiKey}",
                        ticker, apiKey)
                .retrieve()
                .body(ApiResponse.class);
    }

    private ApiResponse fallbackEmpty(String ticker, String apiKey, Throwable ex) {
        var r = new ApiResponse();
        r.setResults(List.of());
        return r;
    }
}
