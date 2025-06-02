package daniel.nuud.company_info_service.repository;

import daniel.nuud.company_info_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    Company findByTickerIgnoreCase(String ticker);
}
