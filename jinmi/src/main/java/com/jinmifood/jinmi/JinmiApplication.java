package com.jinmifood.jinmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling
@SpringBootApplication
public class JinmiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JinmiApplication.class, args);
    }

}
