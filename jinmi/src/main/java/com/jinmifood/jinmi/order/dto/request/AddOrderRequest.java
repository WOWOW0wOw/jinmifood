package com.jinmifood.jinmi.order.dto.request;

import com.jinmifood.jinmi.order.domain.Order;
import com.jinmifood.jinmi.order.domain.OrderStatus;
import com.jinmifood.jinmi.order.domain.PaymentType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AddOrderRequest {

    private Long itemId;
    private Long userId;
    private Integer qty;
    private Integer price;
    private String orderCode;
    private String orderTime;
    private PaymentType paymentType;
    private Integer totalPrice;
    private OrderStatus orderStatus;
    private String itemName;
    private String itemOption;
    private String itemImg;


    public Order toEntity() {
        return Order.builder()
                .itemId(this.itemId)
                .userId(this.userId)
                .qty(this.qty)
                .price(this.price)
                .orderCode(this.orderCode)
                .orderTime(this.orderTime)
                .paymentType(this.paymentType)
                .totalPrice(this.totalPrice)
                .orderStatus(this.orderStatus)
                .itemName(this.itemName)
                .itemOption(this.itemOption)
                .itemImg(this.itemImg)
                .build();

    }
}
