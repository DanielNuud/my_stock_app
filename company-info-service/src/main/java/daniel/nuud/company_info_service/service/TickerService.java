package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.api.Ticker;
import daniel.nuud.company_info_service.dto.api.TickerApiResponse;
import daniel.nuud.company_info_service.model.TickerEntity;
import daniel.nuud.company_info_service.repository.TickerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TickerService {

    private final TickerRepository tickerRepository;
    private final RestClient restClient;

    @Value("${polygon.api.key}")
    private String apiKey;

    @Transactional
    public void fetchAndSaveTickers(String query) {

        TickerApiResponse response = restClient.get()
                .uri("/v3/reference/tickers?market=stocks&search={query}&apiKey={apiKey}", query.toUpperCase(), apiKey)
                .retrieve()
                .body(TickerApiResponse.class);

        if (response != null && response.getResults() != null) {
            List<Ticker> tickerDTOs = response.getResults();

            List<String> tickersToCheck = tickerDTOs.stream()
                    .map(Ticker::getTicker)
                    .toList();

            Set<String> existingTickers = tickerRepository.findAllById(tickersToCheck).stream()
                    .map(TickerEntity::getTicker)
                    .collect(Collectors.toSet());

            List<TickerEntity> newTickers = tickerDTOs.stream()
                    .filter(dto -> !existingTickers.contains(dto.getTicker()))
                    .map(dto -> new TickerEntity(dto.getTicker(), dto.getName(), dto.getCurrencyName()))
                    .toList();

            if (!newTickers.isEmpty()) {
                tickerRepository.saveAll(newTickers);
                log.info("Saved {} new tickers for query {}", newTickers.size(), query);
            } else {
                log.info("No new tickers to save for query {}", query);
            }
        }
    }

    public List<TickerEntity> autocomplete(String query) {
        fetchAndSaveTickers(query);

        return tickerRepository.findTop5ByTickerStartsWithIgnoreCase(query);
    }
}
