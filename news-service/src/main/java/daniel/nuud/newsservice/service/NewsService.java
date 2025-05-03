package daniel.nuud.newsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final WebClient webClient;

    @Value("${polygon.api.key}")
    private String apiKey;

    

}
