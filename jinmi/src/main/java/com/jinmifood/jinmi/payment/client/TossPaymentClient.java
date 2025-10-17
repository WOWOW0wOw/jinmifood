// src/main/java/com/jinmifood/jinmi/payment/client/TossPaymentClient.java
package com.jinmifood.jinmi.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final ObjectMapper objectMapper;

    @Value("${toss.secret-key}")
    private String secretKey; // 예: test_sk_xxxxx

    private String authHeader() {
        String raw = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /** 결제 승인 요청 */
    public HttpResponse<String> requestConfirm(String paymentKey, String orderId, int amount) throws Exception {
        var body = objectMapper.createObjectNode()
                .put("paymentKey", paymentKey)
                .put("orderId", orderId)
                .put("amount", amount);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    }

    /** 결제 취소 요청 */
    public HttpResponse<String> requestCancel(String paymentKey, String cancelReason) throws Exception {
        var body = objectMapper.createObjectNode()
                .put("cancelReason", cancelReason);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    }
}
