package com.jinmifood.jinmi.user.service;

import com.jinmifood.jinmi.user.domain.AccessLog;
import com.jinmifood.jinmi.user.repository.AccessLogRepository;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor

public class AccessLogService {
    private final AccessLogRepository accessLogRepository;
    private final RestTemplate restTemplate; // 외부 api 호출

    public void saveLog(HttpServletRequest request){
        String agentStr = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agentStr);
        String ip = getClientIp(request);

        AccessLog log = AccessLog.builder()
                .ip(ip)
                .browser(userAgent.getBrowser().getName())
                .os(userAgent.getOperatingSystem().getName())
                .device(userAgent.getOperatingSystem().getDeviceType().getName())
                .country(getCountryFromApi(ip)) // API로 국가 조회
                .requestUrl(request.getRequestURI())
                .build();

        accessLogRepository.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
    private String getCountryFromApi(String ip) {
        if (ip.equals("127.0.0.1") || ip.startsWith("192.168")) return "Local";
        try {
            // 외부 API 호출 ip-api.com
            String url = "http://ip-api.com/json/" + ip + "?fields=country";
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            return response != null ? response.get("country") : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
