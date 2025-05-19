package daniel.nuud.historicalanalyticsservice.repository;

import daniel.nuud.historicalanalyticsservice.model.StockBar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockBarRepository extends JpaRepository<StockBar, String> {
}
