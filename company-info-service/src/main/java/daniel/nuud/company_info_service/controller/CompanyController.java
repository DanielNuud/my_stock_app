package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/{ticker}/fetch")
    public Mono<ResponseEntity<Company>> fetchCompany(@PathVariable String ticker) {
        return companyService.fetchAndSaveCompany(ticker)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{ticker}")
    public Mono<ResponseEntity<Company>> getCompany(@PathVariable String ticker) {
        return companyService.getCompanyByTicker(ticker)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
