package com.jinmifood.jinmi.itemCart.dto.response;

import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
public class ViewItemCartResponse {

    private final String itemName;
    private String itemOption;
    private Long totalCnt;
    private Integer price;
    private Integer point;
    private Integer totalPrice;

    public ViewItemCartResponse(ItemCart itemCart) {
        this.itemName = itemCart.getItemName();
        this.itemOption = itemCart.getItemOption();
        this.totalCnt = itemCart.getTotalCnt();
        this.price = itemCart.getPrice();
        this.totalPrice = itemCart.getTotalPrice();
    }
}
