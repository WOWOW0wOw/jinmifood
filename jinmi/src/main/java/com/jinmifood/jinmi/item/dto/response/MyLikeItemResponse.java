package com.jinmifood.jinmi.item.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyLikeItemResponse {
    private Long likeId;
    private Long itemId;
    private String name;
    private int price;
    private String imageUrl;
}
