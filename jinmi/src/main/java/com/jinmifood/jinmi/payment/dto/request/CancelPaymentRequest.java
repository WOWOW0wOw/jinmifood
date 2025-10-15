package com.jinmifood.jinmi.payment.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelPaymentRequest {
    private String paymentKey;
    private String cancelReason;
}