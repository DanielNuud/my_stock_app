package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    @Autowired
    private CompanyService companyService;


    @GetMapping("/{ticker}")
    public ResponseEntity<Company> getCompany(@PathVariable String ticker) {
        Company company = companyService.fetchCompany(ticker);

        return ResponseEntity.ok(company);
    }

}
