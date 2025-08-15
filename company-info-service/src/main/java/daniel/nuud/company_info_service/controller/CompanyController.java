package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.service.CompanyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{ticker}")
    public ResponseEntity<Company> getCompany(@PathVariable String ticker, HttpServletRequest req) {
        log.info("Received GET " + req.getRequestURI());
        boolean refreshed = companyService.tryRefreshCompany(ticker);
        Company company = companyService.getFromDb(ticker);

        return ResponseEntity.ok()
                .header("X-Data-Freshness", refreshed ? "fresh" : "stale")
                .body(company);
    }

}
