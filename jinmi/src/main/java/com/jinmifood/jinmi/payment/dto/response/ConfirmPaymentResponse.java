package com.jinmifood.jinmi.payment.dto.response;

import com.jinmifood.jinmi.payment.domain.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class ConfirmPaymentResponse {
    private String orderId;
    private String paymentKey;
    private Integer amount;
    private PaymentStatus status;
    private String method;
    private String receiptUrl;
    private LocalDateTime approvedAt;
}
