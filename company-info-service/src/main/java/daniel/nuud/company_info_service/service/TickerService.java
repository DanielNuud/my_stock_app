package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.api.Ticker;
import daniel.nuud.company_info_service.dto.api.TickerApiResponse;
import daniel.nuud.company_info_service.exception.ResourceNotFoundException;
import daniel.nuud.company_info_service.model.TickerEntity;
import daniel.nuud.company_info_service.repository.TickerRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TickerService {

    private final TickerRepository tickerRepository;
    private final PolygonClient polygonClient;
    private final TickerWriter tickerWriter;

    private final RestClient restClient;

    @Value("${polygon.api.key}")
    private String apiKey;

    private boolean skipRefresh(String query, Throwable ex) {
        log.warn("Skip refresh for {}: {}", query, ex.toString());
        return false;
    }

    @CacheEvict(value = "tickerSuggest", allEntries = true)
    @Bulkhead(name = "companySearch", fallbackMethod = "skipRefresh")
    public boolean fetchAndSaveTickers(String query) {
        TickerApiResponse response = polygonClient.getTickerApiResponse(query, apiKey);

        if (response == null || response.getResults() == null) {
            return false;
        }

        List<TickerEntity> newTickers = getTickerEntities(response);
        tickerWriter.saveTickers(newTickers);
        return true;
    }

    private List<TickerEntity> getTickerEntities(TickerApiResponse response) {
        List<Ticker> tickerDTOs = response.getResults();

        List<String> tickersToCheck = tickerDTOs.stream()
                .map(Ticker::getTicker)
                .toList();

        Set<String> existingTickers = tickerRepository.findAllById(tickersToCheck).stream()
                .map(TickerEntity::getTicker)
                .collect(Collectors.toSet());

        return tickerDTOs.stream()
                .filter(dto -> !existingTickers.contains(dto.getTicker()))
                .map(dto -> new TickerEntity(dto.getTicker(), dto.getName(), dto.getCurrencyName()))
                .toList();
    }

    @Cacheable(value = "tickerSuggest", key = "#query", sync = true)
    @Bulkhead(name = "companySearch", type = Bulkhead.Type.SEMAPHORE)
    public List<TickerEntity> getFromDB(String query) {
        return tickerRepository.findTop5ByTickerStartsWithIgnoreCase(query)
                .orElseThrow(() -> new ResourceNotFoundException("Ticker with " + query + " not found"));
    }
}
