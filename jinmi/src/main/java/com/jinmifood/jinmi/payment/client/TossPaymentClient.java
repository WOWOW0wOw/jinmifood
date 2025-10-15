package com.jinmifood.jinmi.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinmifood.jinmi.payment.config.TossProperties;
import com.jinmifood.jinmi.payment.dto.request.ConfirmPaymentRequest;
import lombok.RequiredArgsConstructor;
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

    private static final String BASE_URL = "https://api.tosspayments.com/v1";
    private final TossProperties tossProperties;
    private final ObjectMapper objectMapper;

    private String authHeader() {
        String raw = tossProperties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /** 결제 승인 요청 */
    public HttpResponse<String> requestConfirm(ConfirmPaymentRequest req) throws Exception {
        JsonNode body = objectMapper.createObjectNode()
                .put("paymentKey", req.getPaymentKey())
                .put("orderId", req.getOrderId())
                .put("amount", req.getAmount());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/payments/confirm"))
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    /** 결제 취소 */
    public HttpResponse<String> requestCancel(String paymentKey, String cancelReason) throws Exception {
        JsonNode body = objectMapper.createObjectNode()
                .put("cancelReason", cancelReason);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/payments/" + paymentKey + "/cancel"))
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}
