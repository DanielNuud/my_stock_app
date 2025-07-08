package daniel.nuud.company_info_service.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import daniel.nuud.company_info_service.dto.api.Address;
import daniel.nuud.company_info_service.dto.api.ApiResponse;
import daniel.nuud.company_info_service.dto.api.Branding;
import daniel.nuud.company_info_service.dto.api.Ticket;
import daniel.nuud.company_info_service.exception.ResourceNotFoundException;
import daniel.nuud.company_info_service.model.Company;
import daniel.nuud.company_info_service.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class CompanyServiceTest {

    @InjectMocks
    private CompanyService companyService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(anyString(), anyString(), anyString()))
                .thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(ApiResponse.class)).thenReturn(Mono.empty());

        ReflectionTestUtils.setField(companyService, "apiKey", "fake-api-key");
    }

    @Test
    @DisplayName("fetchCompany: if company exists in DB, return it without calling WebClient")
    void fetchCompany_WhenExistsInDb_ReturnsExisting() {

        Company existing = new Company();
        existing.setTicker("AAPL");
        existing.setName("Apple Inc.");
        when(companyRepository.findByTickerIgnoreCase("AAPL")).thenReturn(existing);

        Company result = companyService.fetchCompany("AAPL");

        assertThat(result).isEqualTo(existing);

        verify(webClient, never()).get();

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("fetchCompany: when not in DB and API returns data, saves and returns new Company")
    void fetchCompany_WhenNotInDbAndApiReturnsData_SavesAndReturns() {

        when(companyRepository.findByTickerIgnoreCase("GOOG")).thenReturn(null);

        Address address = new Address();
        address.setCity("Mountain View");
        address.setAddress1("1600 Amphitheatre Parkway");

        Branding branding = new Branding();
        branding.setLogoUrl("https://logo.example.com/google.png");
        branding.setIconUrl("https://icon.example.com/google-icon.png");


        Ticket ticketData = new Ticket();
        ticketData.setTicker("GOOG");
        ticketData.setName("Google LLC");
        ticketData.setHomepageUrl("https://google.com");
        ticketData.setMarketCap(1_000_000_000L);
        ticketData.setPrimaryExchange("NASDAQ");
        ticketData.setAddress(address);
        ticketData.setBranding(branding);

        ApiResponse fakeApiResp = new ApiResponse();
        fakeApiResp.setStatus("OK");
        fakeApiResp.setResults(ticketData);

        when(responseSpec.bodyToMono(ApiResponse.class)).thenReturn(Mono.just(fakeApiResp));

        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company result = companyService.fetchCompany("GOOG");

        assertThat(result.getTicker()).isEqualTo("GOOG");
        assertThat(result.getName()).isEqualTo("Google LLC");
        assertThat(result.getHomepageUrl()).isEqualTo("https://google.com");
        assertThat(result.getMarketCap()).isEqualTo("1000000000");
        assertThat(result.getPrimaryExchange()).isEqualTo("NASDAQ");
        assertThat(result.getCity()).isEqualTo("Mountain View");
        assertThat(result.getAddress1()).isEqualTo("1600 Amphitheatre Parkway");
        assertThat(result.getLogoUrl()).isEqualTo("https://logo.example.com/google.png");
        assertThat(result.getIconUrl()).isEqualTo("https://icon.example.com/google-icon.png");
        assertThat(result.getStatus()).isEqualTo("OK");

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository, times(1)).save(captor.capture());
        Company saved = captor.getValue();
        assertThat(saved.getTicker()).isEqualTo("GOOG");
        assertThat(saved.getName()).isEqualTo("Google LLC");
        assertThat(saved.getCity()).isEqualTo("Mountain View");
        assertThat(saved.getLogoUrl()).isEqualTo("https://logo.example.com/google.png");
    }

    @Test
    @DisplayName("fetchCompany: if API returns null (or getResults() == null), throws ResourceNotFoundException")
    void fetchCompany_WhenApiReturnsNoData_ThrowsException() {

        when(companyRepository.findByTickerIgnoreCase("MSFT")).thenReturn(null);

        ApiResponse respNull = new ApiResponse();
        respNull.setStatus("OK");
        respNull.setResults(null);
        when(responseSpec.bodyToMono(ApiResponse.class)).thenReturn(Mono.just(respNull));

        assertThatThrownBy(() -> companyService.fetchCompany("MSFT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MSFT");

        verify(companyRepository, never()).save(any());
    }
}