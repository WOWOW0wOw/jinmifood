package com.jinmifood.jinmi.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinmifood.jinmi.payment.client.TossPaymentClient;
import com.jinmifood.jinmi.payment.domain.Payment;
import com.jinmifood.jinmi.payment.domain.PaymentStatus;
import com.jinmifood.jinmi.payment.dto.request.CancelPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.ConfirmPaymentRequest;
import com.jinmifood.jinmi.payment.dto.response.ConfirmPaymentResponse;
import com.jinmifood.jinmi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest req, Long userIdIfAny) throws Exception {
        HttpResponse<String> resp = tossPaymentClient.requestConfirm(req);

        if (resp.statusCode() == 200) {
            // 토스 응답 파싱
            JsonNode root = objectMapper.readTree(resp.body());

            String paymentKey = root.path("paymentKey").asText(null);
            String orderId    = root.path("orderId").asText(req.getOrderId());
            int amount        = root.path("totalAmount").asInt(req.getAmount() == null ? 0 : req.getAmount());
            String method     = root.path("method").asText(null);          // 카드/가상계좌/간편결제 등
            String receiptUrl = root.path("receipt").path("url").asText(null); // 없을 수도 있음
            LocalDateTime approvedAt = null;
            String approvedAtStr = root.path("approvedAt").asText(null);
            if (approvedAtStr != null && !approvedAtStr.isBlank()) {
                approvedAt = LocalDateTime.parse(approvedAtStr.replace("Z", ""));
            }

            // DB 저장
            try {
                Payment payment = Payment.builder()
                        .userId(userIdIfAny)
                        .orderId(orderId)
                        .paymentKey(paymentKey)
                        .totalPrice(amount)
                        .status(PaymentStatus.APPROVED)
                        .method(method)
                        .receiptUrl(receiptUrl)
                        .approvedAt(approvedAt)
                        .build();

                paymentRepository.save(payment);
            } catch (DataAccessException dae) {
                // DB 저장 실패 → 결제 취소 시도
                if (paymentKey != null) {
                    tossPaymentClient.requestCancel(paymentKey, "DB save failed");
                }
                throw dae;
            }

            // 응답 DTO
            return ConfirmPaymentResponse.builder()
                    .orderId(orderId)
                    .paymentKey(paymentKey)
                    .amount(amount)
                    .status(PaymentStatus.APPROVED)
                    .method(method)
                    .receiptUrl(receiptUrl)
                    .approvedAt(approvedAt)
                    .build();

        } else {
            // 실패 응답 내용 그대로 전달(로그/메시지)
            JsonNode err = objectMapper.readTree(resp.body());
            String message = err.path("message").asText("Payment confirm failed");
            throw new IllegalStateException("Toss confirm failed(" + resp.statusCode() + "): " + message);
        }
    }

    @Transactional(readOnly = true)
    public ConfirmPaymentResponse getByOrderId(String orderId) {
        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("payment not found: " + orderId));

        return ConfirmPaymentResponse.builder()
                .orderId(p.getOrderId())
                .paymentKey(p.getPaymentKey())
                .amount(p.getTotalPrice())
                .status(p.getStatus())
                .method(p.getMethod())
                .receiptUrl(p.getReceiptUrl())
                .approvedAt(p.getApprovedAt())
                .build();
    }


    @Transactional
    public String cancel(CancelPaymentRequest req) throws Exception {
        HttpResponse<String> resp = tossPaymentClient.requestCancel(req.getPaymentKey(), req.getCancelReason());

        if (resp.statusCode() == 200) {
            // DB 상태 변경
            paymentRepository.findByPaymentKey(req.getPaymentKey()).ifPresent(p ->
                    paymentRepository.save(p.toBuilder().status(PaymentStatus.CANCELED).build())
            );
            return "CANCELED";
        } else {
            JsonNode err = objectMapper.readTree(resp.body());
            String message = err.path("message").asText("Payment cancel failed");
            throw new IllegalStateException("Toss cancel failed(" + resp.statusCode() + "): " + message);
        }
    }
}
