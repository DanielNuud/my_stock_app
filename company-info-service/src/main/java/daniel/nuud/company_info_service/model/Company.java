package daniel.nuud.company_info_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Company {

    @Id
    private String ticker;


}
