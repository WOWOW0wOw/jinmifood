package com.jinmifood.jinmi.order.dto.response;

import com.jinmifood.jinmi.order.domain.Order;
import com.jinmifood.jinmi.order.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ViewOrderResponse {

    private String itemName;
    private String itemOption;
    private Integer price;
    private Integer totalPrice;
    private Integer qty;
    private OrderStatus orderStatus;
    private String orderCode;
    private String orderTime;

    public ViewOrderResponse(Order order) {
        this.itemName = order.getItemName();
        this.itemOption = order.getItemOption();
        this.price = order.getPrice();
        this.totalPrice = order.getTotalPrice();
        this.qty = order.getQty();
        this.orderStatus = order.getOrderStatus();
        this.orderCode = order.getOrderCode();
        this.orderTime = order.getOrderTime();
    }
}
