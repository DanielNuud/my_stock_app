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
        company.setTicker(data.getTicker());
        company.setName(data.getName());
//            company.setDescription(data.getDescription());
        company.setHomepageUrl(data.getHomepageUrl());
        company.setCity(data.getAddress().getCity());
        company.setAddress1(data.getAddress().getAddress1());
        String lowerCaseTicker = data.getTicker().toLowerCase();
        String logoUrl = "https://cdn.polygon.io/logos/" + lowerCaseTicker + "/logo.png";
        String iconUrl = "https://cdn.polygon.io/logos/" + lowerCaseTicker + "/icon.png";
        company.setLogoUrl(logoUrl);
        company.setIconUrl(iconUrl);
        company.setMarketCap(data.getMarketCap());
        company.setPrimaryExchange(data.getPrimaryExchange());
        company.setStatus(response.getStatus());

        companyRepository.save(company);

        return company;
    }

}
