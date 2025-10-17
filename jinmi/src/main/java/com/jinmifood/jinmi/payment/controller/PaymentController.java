// src/main/java/com/jinmifood/jinmi/payment/controller/PaymentController.java
package com.jinmifood.jinmi.payment.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.payment.dto.request.CancelPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.ConfirmPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.PreparePaymentRequest;
import com.jinmifood.jinmi.payment.dto.response.ConfirmPaymentResponse;
import com.jinmifood.jinmi.payment.dto.response.PaymentResponse;
import com.jinmifood.jinmi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 1) 결제 준비
     *  - 서버가 금액 확정, orderId 생성(PENDING 저장), 프론트에 전달
     */
    @PostMapping("/prepare")
    public StatusResponseDTO prepare(@Valid @RequestBody(required = false) PreparePaymentRequest req,
                                     @AuthenticationPrincipal(expression = "id") Long userId) {
        PaymentResponse result = paymentService.prepare(req, userId);
        return StatusResponseDTO.ok(result);
    }

    /**
     * 2) 결제 승인(토스 confirm)
     */
    @PostMapping("/confirm")
    public StatusResponseDTO confirmPayment(@Valid @RequestBody ConfirmPaymentRequest req,
                                            @AuthenticationPrincipal(expression = "id") Long userId) throws Exception {
        ConfirmPaymentResponse result = paymentService.confirm(req, userId);
        return StatusResponseDTO.ok(result);
    }

    /**
     * 3) 단건 조회
     */
    @GetMapping("/{orderId}")
    public StatusResponseDTO getPayment(@PathVariable String orderId) {
        ConfirmPaymentResponse payment = paymentService.getByOrderId(orderId);
        return StatusResponseDTO.ok(payment);
    }

    /**
     * 4) 결제 취소
     */
    @PostMapping("/cancel")
    public StatusResponseDTO cancel(@Valid @RequestBody CancelPaymentRequest req) throws Exception {
        String status = paymentService.cancel(req);
        return StatusResponseDTO.ok(status);
    }
}
