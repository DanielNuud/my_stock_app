package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.model.TickerEntity;
import daniel.nuud.company_info_service.repository.TickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TickerWriter {
    private final TickerRepository tickerRepository;

    @Transactional(timeout = 3)
    public void saveTickers(List<TickerEntity> tickers) {
        tickerRepository.saveAll(tickers);
    }
}
