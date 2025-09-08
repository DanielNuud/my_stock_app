package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Companies", description = "Company info API")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @Operation(
            summary = "Get company by ticker",
            description = "Returns company profile",
            tags = {"Companies"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/{ticker}")
    public ResponseEntity<Company> getCompany(
            @Parameter(
            description = "Stock ticker symbol",
            example = "AAPL",
            required = true
            )
            @PathVariable String ticker, HttpServletRequest req) {

        log.info("Received GET {}", req.getRequestURI());
        boolean refreshed = companyService.tryRefreshCompany(ticker);
        Company company = companyService.getFromDb(ticker);

        return ResponseEntity.ok()
                .header("X-Data-Freshness", refreshed ? "fresh" : "stale")
                .body(company);
    }

}
