package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.api.ApiResponse;
import daniel.nuud.company_info_service.dto.api.Ticket;
import daniel.nuud.company_info_service.exception.ResourceNotFoundException;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {

    private final PolygonClient polygonClient;
    private final CompanyRepository companyRepository;
    private final CompanyWriter companyWriter;

    @Value("${polygon.api.key}")
    private String apiKey;

    private boolean skipRefresh(String ticker, Throwable ex) {
        log.warn("Skip refresh for {}: {}", ticker, ex.toString());
        return false;
    }

    @Bulkhead(name = "companyWrite", fallbackMethod = "skipRefresh")
    public boolean tryRefreshCompany(String ticker) {
//        Company existingCompany = companyRepository.findByTickerIgnoreCase(ticker);
//
//        if (existingCompany != null) {
//            log.info("Company {} found in database", ticker);
//            return existingCompany;
//        }
        ApiResponse response = polygonClient.getApiResponse(ticker, apiKey);

        if (response == null || response.getResults() == null) {
            return false;
        }

        Company company = mapToCompany(response, ticker);
        companyWriter.saveCompany(company);
        return true;
    }

    public Company fetchCompany(String ticker) {
        tryRefreshCompany(ticker);
        return getFromDb(ticker);
    }

    @Cacheable(value = "companyByTicker", key = "#ticker.toUpperCase()", sync = true)
    @Bulkhead(name = "companyRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true, timeout = 2)
    public Company getFromDb(String ticker) {
        var key = ticker.toUpperCase().trim();
        return companyRepository.findByTickerIgnoreCase(key)
                .orElseThrow(() -> new ResourceNotFoundException("Company with " + ticker + " not found"));
    }

    private Company mapToCompany(ApiResponse response, String ticker) {
        Ticket data = response.getResults();

        Company company = new Company();

        company.setTicker(defaultIfNull(ticker.trim().toUpperCase(), "Not found"));
        company.setName(defaultIfNull(data.getName(), "Not found"));
        company.setDescription(defaultIfNull(data.getDescription(), "Not found"));
        company.setHomepageUrl(defaultIfNull(data.getHomepageUrl(), "Not found"));
        company.setCity(data.getAddress() != null ? defaultIfNull(data.getAddress().getCity(), "Not found") : "Not found");
        company.setAddress1(data.getAddress() != null ? defaultIfNull(data.getAddress().getAddress1(), "Not found") : "Not found");
        company.setLogoUrl(data.getBranding() != null ? defaultIfNull(data.getBranding().getLogoUrl(), "Not found") : "Not found");
        company.setIconUrl(data.getBranding() != null ? defaultIfNull(data.getBranding().getIconUrl(), "Not found") : "Not found");
        company.setMarketCap(defaultIfNull(String.valueOf(data.getMarketCap()), "Not found"));
        company.setPrimaryExchange(defaultIfNull(data.getPrimaryExchange(), "Not found"));
        company.setStatus(defaultIfNull(response.getStatus(), "Not found"));

        companyRepository.save(company);

        return company;
    }

    private String defaultIfNull(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

}
