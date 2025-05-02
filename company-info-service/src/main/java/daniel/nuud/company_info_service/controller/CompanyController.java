package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/{ticker}/fetch")
    public ResponseEntity<Company> fetchCompany(@PathVariable String ticker) {
        return ResponseEntity.ok(companyService.fetchAndSaveCompany(ticker).block());
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<Company> getCompany(@PathVariable String ticker) {
        Company company = companyService.getCompanyByTicker(ticker);
        return ResponseEntity.ok(company);
    }
}
