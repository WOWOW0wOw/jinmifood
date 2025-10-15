package com.jinmifood.jinmi.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class PaymentConfig {
}
