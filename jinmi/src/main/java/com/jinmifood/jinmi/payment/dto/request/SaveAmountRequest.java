package com.jinmifood.jinmi.payment.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAmountRequest {
    public String orderId;
    public String amount;
}
