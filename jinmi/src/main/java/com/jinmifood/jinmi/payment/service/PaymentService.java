// src/main/java/com/jinmifood/jinmi/payment/service/PaymentService.java
package com.jinmifood.jinmi.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import com.jinmifood.jinmi.itemCart.repository.ItemCartRepository;
import com.jinmifood.jinmi.payment.client.TossPaymentClient;
import com.jinmifood.jinmi.payment.domain.Payment;
import com.jinmifood.jinmi.payment.domain.PaymentStatus;
import com.jinmifood.jinmi.payment.dto.request.CancelPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.ConfirmPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.PreparePaymentRequest;
import com.jinmifood.jinmi.payment.dto.response.ConfirmPaymentResponse;
import com.jinmifood.jinmi.payment.dto.response.PaymentResponse;
import com.jinmifood.jinmi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final ItemCartRepository itemCartRepository;

    /**
     * 결제 준비: 금액 확정 → orderId 생성 → PENDING 저장 → 프론트 반환
     */
    @Transactional
    public PaymentResponse prepare(PreparePaymentRequest req, Long userId) {
        if (req.getCartIds() == null || req.getCartIds().isEmpty()) {
            throw new IllegalArgumentException("cartIds가 비어있습니다.");
        }

        // 1) 선택된 장바구니 로드
        List<ItemCart> carts = itemCartRepository.findAllByUserIdAndIdIn(userId, req.getCartIds());
        if (carts.isEmpty()) throw new IllegalArgumentException("선택한 장바구니를 찾을 수 없습니다.");

        // 2) 합계 계산 (price * qty)
        int amount = carts.stream()
                .mapToInt(c -> (c.getPrice() != null ? c.getPrice() : 0) * (c.getTotalCnt() != null ? c.getTotalCnt() : 1))
                .sum();
        if (amount <= 0) throw new IllegalStateException("결제 금액이 유효하지 않습니다.");

        // 3) 주문명 결정: 첫 상품 + 외 N건
        String orderName = req.getOrderName();
        if (orderName == null || orderName.isBlank()) {
            ItemCart first = carts.get(0);
            String base = first.getItemName() + (first.getItemOption() != null ? " (" + first.getItemOption() + ")" : "");
            orderName = (carts.size() == 1) ? base : base + " 외 " + (carts.size() - 1) + "건";
        }

        // 4) 서버 주문번호 생성 (고유)
        String orderId = "ORD-" + createOrderCode();

        // 5) 결제(또는 주문) 임시 레코드 저장 (서버가 확정 금액을 보관)
        Payment pending = Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .orderName(orderName)
                .totalPrice(amount)
                .status(PaymentStatus.PENDING)  // 또는 READY
                .build();
        paymentRepository.save(pending);

        // 6) 프론트로 반환 (프론트는 이 값으로 toss.requestPayment 호출)
        return PaymentResponse.builder()
                .orderId(orderId)
                .amount(amount)
                .orderName(orderName)
                .build();
    }

    public String createOrderCode(){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String randomString = "";
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            randomString += alphabet.charAt(random.nextInt(alphabet.length()));
            randomString += numbers.charAt(random.nextInt(numbers.length()));
        }
        String code = randomString + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        log.info("code : {}", code);
        return code;
    }

    /**
     * 결제 승인
     *  - DB의 금액으로 토스 승인 요청(프론트 금액 신뢰 X)
     *  - 성공 시 APPROVED로 업데이트
     *  - DB 저장 실패 시 즉시 결제 취소
     */
    @Transactional
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest req, Long userIdIfAny) throws Exception {
        // 1) 서버 금액/주문 조회
        Payment prepared = paymentRepository.findByOrderId(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("준비된 주문을 찾을 수 없습니다: " + req.getOrderId()));

        int amount = prepared.getTotalPrice(); // 서버 확정 금액

        // 2) 토스 승인 요청
        HttpResponse<String> resp = tossPaymentClient.requestConfirm(req.getPaymentKey(), req.getOrderId(), amount);

        if (resp.statusCode() != 200) {
            JsonNode err = objectMapper.readTree(resp.body());
            String message = err.path("message").asText("Payment confirm failed");
            throw new IllegalStateException("Toss confirm failed(" + resp.statusCode() + "): " + message);
        }

        // 3) 응답 파싱
        JsonNode root = objectMapper.readTree(resp.body());
        String paymentKey = root.path("paymentKey").asText(null);
        String method     = root.path("method").asText(null);
        String receiptUrl = root.path("receipt").path("url").asText(null);

        LocalDateTime approvedAt = null;
        String approvedAtStr = root.path("approvedAt").asText(null);
        if (approvedAtStr != null && !approvedAtStr.isBlank()) {
            approvedAt = OffsetDateTime.parse(approvedAtStr)
                    .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
        }

        // 4) DB 반영
        try {
            Payment updated = prepared.toBuilder()
                    .paymentKey(paymentKey)
                    .status(PaymentStatus.APPROVED)
                    .method(method)
                    .receiptUrl(receiptUrl)
                    .approvedAt(approvedAt)
                    .build();
            paymentRepository.save(updated);
        } catch (DataAccessException dae) {
            // DB 저장 실패 시 결제를 즉시 취소
            if (paymentKey != null) {
                tossPaymentClient.requestCancel(paymentKey, "DB save failed");
            }
            throw dae;
        }

        // 5) 응답
        return ConfirmPaymentResponse.builder()
                .orderId(prepared.getOrderId())
                .paymentKey(paymentKey)
                .price(amount)
                .status(PaymentStatus.APPROVED)
                .method(method)
                .receiptUrl(receiptUrl)
                .approvedAt(approvedAt)
                .build();
    }

    @Transactional(readOnly = true)
    public ConfirmPaymentResponse getByOrderId(String orderId) {
        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("payment not found: " + orderId));

        return ConfirmPaymentResponse.builder()
                .orderId(p.getOrderId())
                .paymentKey(p.getPaymentKey())
                .price(p.getTotalPrice())
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

    // ===== 내부 유틸 =====
}
