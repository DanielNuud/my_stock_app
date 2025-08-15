package daniel.nuud.company_info_service.repository;

import daniel.nuud.company_info_service.model.TickerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TickerRepository extends JpaRepository<TickerEntity, String> {
    Optional<List<TickerEntity>> findTop5ByTickerStartsWithIgnoreCase(String tickerStart);
}
