package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.api.ApiResponse;
import daniel.nuud.company_info_service.dto.api.Ticket;
import daniel.nuud.company_info_service.exception.ResourceNotFoundException;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {

    private final WebClient webClient;

    private final CompanyRepository companyRepository;

    @Value("${polygon.api.key}")
    private String apiKey;

    @Cacheable(value = "Company", key = "#ticker.toUpperCase()")
    public Company fetchCompany(String ticker) {

        log.info(">>> fetchCompany called for {}", ticker);

        Company existingCompany = companyRepository.findByTickerIgnoreCase(ticker);

        if (existingCompany != null) {
            log.info("Company {} found in database", ticker);
            return existingCompany;
        }

        ApiResponse response = webClient.get()
                .uri("/v3/reference/tickers/{ticker}?apiKey={apiKey}", ticker.toUpperCase(), apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();

        if (response == null || response.getResults() == null) {
            throw new ResourceNotFoundException("Company with ticker: " + ticker + " not found");
        }

        return getCompany(response);

    }

    public Company getCompany(ApiResponse response) {
        Ticket data = response.getResults();

        Company company = new Company();
        company.setTicker(defaultIfNull(data.getTicker(), "Not found"));
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
