 package daniel.nuud.company_info_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

 @SpringBootApplication
 @EnableCaching
public class CompanyInfoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyInfoServiceApplication.class, args);
    }

}
