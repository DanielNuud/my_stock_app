package daniel.nuud.company_info_service.service;

import daniel.nuud.company_info_service.dto.ApiResponse;
import daniel.nuud.company_info_service.dto.Ticket;
import daniel.nuud.company_info_service.exception.ResourceNotFoundException;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final WebClient webClient;
    private final CompanyRepository companyRepository;
    @Value("${polygon.api.key}")
    private String apiKey;

    public Company fetchAndSaveCompany(String ticker) {
        ApiResponse response = webClient.get()
                .uri("/v3/reference/tickers/{ticker}?apiKey={apiKey}", ticker, apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();

        if (response != null && response.getResults() != null) {

            if (companyRepository.existsById(ticker)) {
                return companyRepository.findById(ticker).get();
            } else {
                Company company = getCompany(response);
                return companyRepository.save(company);
            }

        } else {
            throw new ResourceNotFoundException("Company with ticker: " + ticker +" not found");
        }

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

        return company;
    }

    public Company getCompanyByTicker(String ticker) {
        return companyRepository.findById(ticker).orElseThrow(() ->
                new ResourceNotFoundException("Company with ticker: " + ticker +" not found"));
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

}
