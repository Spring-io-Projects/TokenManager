package com.peru.reniecservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ReniecserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReniecserviceApplication.class, args);

        log.info("Swagger UI is available at » http://localhost:8080/swagger-ui/index.html");
        log.info("Console H2 is available at » http://localhost:8080/h2-console");
    }

}
