package daniel.nuud.historicalanalyticsservice.repository;

import daniel.nuud.historicalanalyticsservice.model.StockBar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockBarRepository extends JpaRepository<StockBar, String> {
    @Query("SELECT sb FROM StockBar sb WHERE sb.id.ticker = :ticker AND sb.id.date BETWEEN :startDate AND :endDate")
    List<StockBar> findByTickerAndDateRange(
            @Param("ticker") String ticker,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
