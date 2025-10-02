package com.jinmifood.jinmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JinmiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JinmiApplication.class, args);
    }

}
