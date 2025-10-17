// src/main/java/com/jinmifood/jinmi/payment/dto/request/PreparePaymentRequest.java
package com.jinmifood.jinmi.payment.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparePaymentRequest {
    // 장바구니 기반 계산 시 사용(선택)
    private List<Long> cartIds;
    private String orderName;     // 화면에 노출할 주문명
}
