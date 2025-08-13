package daniel.nuud.stocksservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://historical-service:8080")
                .build();
    }

    @Bean
    public RestClient restClientCurrency() {
        return RestClient.builder()
                .baseUrl("http://currency-service:8080")
                .build();
    }

    @Bean
    public RestClient notificationRestClient() {
        return RestClient.builder()
                .baseUrl("http://notification-service:8080")
                .build();
    }
}
