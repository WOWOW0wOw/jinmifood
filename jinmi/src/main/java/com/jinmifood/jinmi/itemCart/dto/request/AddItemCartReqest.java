package com.jinmifood.jinmi.itemCart.dto.request;

import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AddItemCartReqest {

    private Long itemId;
    private Long userId;
    private String itemName;
    private String itemOption;
    private Integer totalCnt;
    private Integer price;

    public ItemCart toEntity(){
        return ItemCart.builder()
                .itemId(this.itemId)
                .userId(this.userId)
                .itemName(this.itemName)
                .itemOption(this.itemOption)
                .totalCnt(this.totalCnt)
                .price(this.price)
                .build();
    }

}
