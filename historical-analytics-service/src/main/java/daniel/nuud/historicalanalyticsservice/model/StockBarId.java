package daniel.nuud.historicalanalyticsservice.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBarId implements Serializable {

    private String ticker;
    private LocalDate date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockBarId)) return false;
        StockBarId that = (StockBarId) o;
        return Objects.equals(getTicker(), that.getTicker()) &&
                Objects.equals(getDate(), that.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTicker(), getDate());
    }
}
