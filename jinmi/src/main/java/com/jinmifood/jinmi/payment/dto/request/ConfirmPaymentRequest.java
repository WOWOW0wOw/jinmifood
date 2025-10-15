package com.jinmifood.jinmi.payment.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentRequest {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
