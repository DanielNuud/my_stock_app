package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.ApiResponse;
import daniel.nuud.company_info_service.dto.Ticket;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final WebClient webClient;
    private final CompanyRepository companyRepository;

    public Mono<Company> fetchAndSaveCompany(String ticker) {
        return webClient.get()
                .uri("/v3/reference/tickers/{ticker}?apiKey={apiKey}", ticker)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .flatMap(response -> {
                    if (response.getResults() != null) {
                        Ticket data = response.getResults();

                        Company company = new Company();

                        company.setCity(response.getResults().getAddress().getCity());
                        company.setDescription(response.getResults().getDescription());
                        company.setName(response.getResults().getName());
                        company.setIconUrl(response.getResults().getBranding().getIconUrl());
                        company.setLogoUrl(response.getResults().getBranding().getLogoUrl());
                        company.setTicker(response.getResults().getTicker());
                        company.setAddress1(response.getResults().getAddress().getAddress1());
                        company.setMarketCap(response.getResults().getMarketCap());
                        company.setPrimaryExchange(response.getResults().getPrimaryExchange());

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
