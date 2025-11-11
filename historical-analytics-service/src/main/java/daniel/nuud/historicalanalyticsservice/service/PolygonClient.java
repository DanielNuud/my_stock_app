package daniel.nuud.historicalanalyticsservice.service;

import daniel.nuud.historicalanalyticsservice.dto.ApiResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolygonClient {

    private final RestClient polygonRestClient;

//    @CircuitBreaker(name = "polygonAggsCB", fallbackMethod = "fallbackNull")
//    @Retry(name = "readSafe")
//    @Bulkhead(name = "polygonAggsBH", type = Bulkhead.Type.SEMAPHORE)
//    @RateLimiter(name = "polygonAggsRate")
    public ApiResponse getApiResponse(String uri) {
        log.info(uri);
        return polygonRestClient.get()
                .uri(uri)
                .retrieve()
                .body(ApiResponse.class);
    }

    ApiResponse fallbackNull(String uri, Throwable ex) {
        log.warn("Polygon fallback for {}: {}", uri, ex.toString());
        return null;
    }
}
