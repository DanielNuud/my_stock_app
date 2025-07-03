package daniel.nuud.company_info_service.repository;

import daniel.nuud.company_info_service.model.TickerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TickerRepository extends JpaRepository<TickerEntity, String> {
    List<TickerEntity> findTop5ByTickerIgnoreCaseContaining(String tickerStart);
    List<TickerEntity> findTop5ByTickerStartsWithIgnoreCase(String tickerStart);
}
