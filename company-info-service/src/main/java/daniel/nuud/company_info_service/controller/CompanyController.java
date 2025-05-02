package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping("/{ticker}/fetch")
    public ResponseEntity<Company> fetchCompany(@PathVariable String ticker) {
        return ResponseEntity.ok(companyService.fetchAndSaveCompany(ticker));
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<Company> getCompany(@PathVariable String ticker) {
        Company company = companyService.getCompanyByTicker(ticker);
        return ResponseEntity.ok(company);
    }

    @GetMapping()
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }
}
