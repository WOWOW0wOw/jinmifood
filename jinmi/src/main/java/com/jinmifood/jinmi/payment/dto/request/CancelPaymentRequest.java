package com.jinmifood.jinmi.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelPaymentRequest {
    @NotBlank
    private String paymentKey;
    @NotBlank
    private String cancelReason;
}
