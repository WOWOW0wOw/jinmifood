package com.jinmifood.jinmi.item.dto.request;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.domain.ItemStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AddItemRequest {

    private String itemName;
    private Long categoryId;
    private int itemPrice;
    private int itemWeight;
    private int count;


public Item toEntity() {
    return Item.builder()
            .itemName(this.itemName)
            .categoryId(this.categoryId)
            .itemPrice(this.itemPrice)
            .itemWeight(this.itemWeight)
            .count(this.count)
            .build();
}

}
