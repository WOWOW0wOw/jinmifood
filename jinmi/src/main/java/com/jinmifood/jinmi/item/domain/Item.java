package com.jinmifood.jinmi.item.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jinmifood.jinmi.item.dto.request.UpdateItemRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "item")
@ToString()
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name =  "itemId", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Long categoryId; // 카테고리테이블 참조

    @Column(nullable = false)
    private int itemPrice;

    private int orderCnt;

    private int likeCnt;

    private int reviewCnt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    private int itemWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(nullable = false)
    private int count;

    @JsonManagedReference  // 정방향 참조: 정상적으로 직렬화
    @Builder.Default
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true) // orphanRemoval = true: Item의 images 리스트에서 ItemImage가 제거되면 데이터베이스에서도 삭제됨
    private List<ItemImage> images = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        this.createAt = this.createAt == null ? LocalDateTime.now() : this.createAt;
        this.updateAt = this.updateAt == null ? LocalDateTime.now() : this.updateAt;
        this.status = this.count == 0 ? ItemStatus.SOLDOUT : ItemStatus.SALE;
    }

    public void addImage(ItemImage image) {
        images.add(image);
        image.setItem(this);
    }

    public void updateItemDetails(UpdateItemRequest request) {
        if (request.getItemName() != null) {
            this.itemName = request.getItemName();
        }
        if (request.getItemPrice() != null) {
            this.itemPrice = request.getItemPrice();
        }
        if (request.getItemWeight() != null) {
            this.itemWeight = request.getItemWeight();
        }
        if (request.getCount() != null) {
            this.count = request.getCount();
            this.status = this.count == 0 ? ItemStatus.SOLDOUT : ItemStatus.SALE; // 재고 변경 시 상태 업데이트
        }
        this.updateAt = LocalDateTime.now(); // 수정 시각 업데이트
    }

    public void updateItemLikeCnt(){
        this.likeCnt++;
    }

    public void decreaseItemLikeCnt(){
        if(this.likeCnt > 0){
            this.likeCnt--;
        }
    }



    public void updateItemReviewCnt(){
        this.reviewCnt++;
    }

    public void decreaseItemReviewCnt(){
        if(this.reviewCnt > 0){
            this.reviewCnt--;
        }
    }

    public void updateItemOrderCnt(){
        this.orderCnt++;
    }

    public void decreaseItemOrderCnt(){
        if(this.orderCnt > 0){
            this.orderCnt--;
        }
    }

    public void updateReviewCnt(){ this.reviewCnt++; }

    public void decreaseReviewCnt(){
        if(this.reviewCnt > 0){
            this.reviewCnt--;
        }
    }


}
