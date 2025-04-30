package daniel.nuud.company_info_service.repository;

import daniel.nuud.company_info_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
}
