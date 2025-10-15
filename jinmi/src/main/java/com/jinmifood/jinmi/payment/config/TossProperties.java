package com.jinmifood.jinmi.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "toss")
public class TossProperties {
    private String secretKey;
}
