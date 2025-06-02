package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.ApiResponse;
import daniel.nuud.company_info_service.dto.Ticket;
import daniel.nuud.company_info_service.exception.ResourceNotFoundException;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class CompanyService {

    private final WebClient webClient;

    @Autowired
    public CompanyService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${polygon.api.key}")
    private String apiKey;

    @Autowired
    private CompanyRepository companyRepository;

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
        company.setTicker(data.getTicker());
        company.setName(data.getName());
//            company.setDescription(data.getDescription());
        company.setHomepageUrl(data.getHomepageUrl());
        company.setCity(data.getAddress().getCity());
        company.setAddress1(data.getAddress().getAddress1());
        company.setLogoUrl(data.getBranding().getLogoUrl());
        company.setIconUrl(data.getBranding().getIconUrl());
        company.setMarketCap(data.getMarketCap());
        company.setPrimaryExchange(data.getPrimaryExchange());
        company.setStatus(response.getStatus());

        companyRepository.save(company);

        return company;
    }

}
