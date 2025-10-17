package com.jinmifood.jinmi.payment.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String orderId;
    private Integer amount;
    private String orderName;
}