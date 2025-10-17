// src/main/java/com/jinmifood/jinmi/payment/dto/request/ConfirmPaymentRequest.java
package com.jinmifood.jinmi.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentRequest {
    @NotBlank
    private String orderId;     // prepare에서 만든 고정 주문번호
    @NotBlank
    private String paymentKey;  // 토스에서 넘겨준 키

    // 프론트에서 넘어오긴 하지만 서버는 신뢰하지 않고 DB의 금액으로 승인 요청 보냄(옵션)
    private Long amount;
}
