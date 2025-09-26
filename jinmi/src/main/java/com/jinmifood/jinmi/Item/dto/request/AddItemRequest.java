package com.jinmifood.jinmi.Item.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AddItemRequest {

    private int itemId;
    private String itemName;
    private int categoryId;
    private int itemPrice;
    private int orderCnt;
    private int likeCnt;
    private int reviewCnt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String itemImg;
    private String itemInfImg;
    private int itemWeight;
    private String role;
    private int count;

}
