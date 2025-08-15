package daniel.nuud.company_info_service.repository;

import daniel.nuud.company_info_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByTickerIgnoreCase(String ticker);
}
