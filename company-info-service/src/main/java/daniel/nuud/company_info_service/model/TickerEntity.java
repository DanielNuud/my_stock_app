package daniel.nuud.company_info_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Ticker",
        description = "Basic ticker record used for search results."
)
public class TickerEntity {

    @Id
    private String ticker;
    private String companyName;
    private String currency;
}
