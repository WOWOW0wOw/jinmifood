package com.jinmifood.jinmi.user.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "access_logs")
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ip;
    private String browser;
    private String os;
    private String device;
    private String country;
    private String requestUrl;
    private LocalDateTime accessTime;

    @Builder
    public AccessLog(String ip, String browser, String os, String device, String country, String requestUrl) {
        this.ip = ip;
        this.browser = browser;
        this.os = os;
        this.device = device;
        this.country = country;
        this.requestUrl = requestUrl;
        this.accessTime = LocalDateTime.now();
    }
}
