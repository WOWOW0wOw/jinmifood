package com.jinmifood.jinmi.Item.dto.response;

import com.jinmifood.jinmi.Item.domain.Item;
import com.jinmifood.jinmi.Item.domain.itemStatus;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ViewItemResponse {

    private Long itemId; // 엔티티의 id -> itemId로 변경 (클라이언트에게 더 명확)
    private String itemName;
    private int categoryId;
    private int itemPrice;
    private int orderCnt;
    private int likeCnt;
    private int reviewCnt;
    private String itemImg;
    private String itemInfImg;
    private int itemWeight;
    private itemStatus status;
    private int count;
    private LocalDateTime createAt;

    public ViewItemResponse(Item item) {
        this.itemId = item.getId();
        this.itemName = item.getItemName();
        this.categoryId = item.getCategoryId();
        this.itemPrice = item.getItemPrice();
        this.orderCnt = item.getOrderCnt();
        this.likeCnt = item.getLikeCnt();
        this.reviewCnt = item.getReviewCnt();
        this.itemImg = item.getItemImg();
        this.itemInfImg = item.getItemInfImg();
        this.itemWeight = item.getItemWeight();
        this.status = item.getStatus();
        this.count = item.getCount();
        this.createAt = item.getCreateAt();
    }
}
