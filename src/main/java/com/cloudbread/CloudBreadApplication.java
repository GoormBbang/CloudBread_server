package com.cloudbread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CloudBreadApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudBreadApplication.class, args);
    }

}
