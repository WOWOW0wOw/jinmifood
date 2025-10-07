package com.jinmifood.jinmi.item.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UpdateItemRequest {

    private String itemName;
    private Integer itemPrice;
    private String itemImg;
    private String itemInfImg;
    private Integer itemWeight;
    private Integer count;

}
