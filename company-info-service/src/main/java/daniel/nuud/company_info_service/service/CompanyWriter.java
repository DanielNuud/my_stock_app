package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyWriter {

    private final CompanyRepository companyRepository;

    @Transactional(timeout = 3)
    public void saveCompany(Company company) {
        companyRepository.save(company);
    }
}
