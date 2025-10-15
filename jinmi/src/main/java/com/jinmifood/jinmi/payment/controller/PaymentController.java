package com.jinmifood.jinmi.payment.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.payment.dto.request.CancelPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.ConfirmPaymentRequest;
import com.jinmifood.jinmi.payment.dto.request.SaveAmountRequest;
import com.jinmifood.jinmi.payment.dto.response.ConfirmPaymentResponse;
import com.jinmifood.jinmi.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** (선택) 결제 전 금액 세션에 임시 저장 */
    @PostMapping("/saveAmount")
    public StatusResponseDTO saveAmount(HttpSession session, @RequestBody SaveAmountRequest req) {
        session.setAttribute(req.getOrderId(), req.getAmount());
        return StatusResponseDTO.ok("Payment temp save successful");
    }

    /** (선택) 결제 후 금액 검증 */
    @PostMapping("/verifyAmount")
    public StatusResponseDTO verifyAmount(HttpSession session, @RequestBody SaveAmountRequest req) {
        String amount = (String) session.getAttribute(req.getOrderId());
        if (amount == null || !amount.equals(req.getAmount())) {
            return StatusResponseDTO.ok("결제 금액 정보가 유효하지 않습니다.");
        }
        session.removeAttribute(req.getOrderId());
        return StatusResponseDTO.ok("Payment is valid");
    }

    /** 결제 승인 */
    @PostMapping("/confirm")
    public StatusResponseDTO confirmPayment(@RequestBody ConfirmPaymentRequest req) throws Exception {
        // 로그인 기반이면 SecurityContext에서 유저 ID 얻어와서 넣어도 됨
        Long userId = null; // 필요 시 채우기
        ConfirmPaymentResponse result = paymentService.confirm(req, userId);
        return StatusResponseDTO.ok(result);
    }

    /** 단건 조회 (orderId 기준) */
    @GetMapping("/{orderId}")
    public StatusResponseDTO getPayment(@PathVariable String orderId) {
        ConfirmPaymentResponse payment = paymentService.getByOrderId(orderId);
        return StatusResponseDTO.ok(payment);
    }

    /** 결제 취소 */
    @PostMapping("/cancel")
    public StatusResponseDTO cancel(@RequestBody CancelPaymentRequest req) throws Exception {
        String status = paymentService.cancel(req);
        return StatusResponseDTO.ok(status);
    }
}
