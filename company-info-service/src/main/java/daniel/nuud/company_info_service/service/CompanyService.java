package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.ApiResponse;
import daniel.nuud.company_info_service.dto.Ticket;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final WebClient webClient;
    private final CompanyRepository companyRepository;
    @Value("${polygon.api.key}")
    private String apiKey;

    public Mono<Company> fetchAndSaveCompany(String ticker) {
        return webClient.get()
                .uri("/v3/reference/tickers/{ticker}?apiKey={apiKey}", ticker, apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .flatMap(response -> {
                    if (response.getResults() != null) {
                        Ticket data = response.getResults();

                        Company company = new Company();

                        company.setCity(data.getAddress().getCity());
                        company.setName(data.getName());
                        company.setIconUrl(data.getBranding().getIconUrl());
                        company.setLogoUrl(data.getBranding().getLogoUrl());
                        company.setTicker(data.getTicker());
                        company.setAddress1(data.getAddress().getAddress1());
                        company.setMarketCap(data.getMarketCap());
                        company.setPrimaryExchange(data.getPrimaryExchange());

                        return Mono.fromCallable(() -> companyRepository.save(company));
                    } else {
                        return Mono.error(new RuntimeException("Company not found"));
                    }

                });
    }

    public Mono<Company> getCompanyByTicker(String ticker) {
        return Mono.justOrEmpty(companyRepository.findById(ticker));
    }
}
