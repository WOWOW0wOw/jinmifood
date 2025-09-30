package com.jinmifood.jinmi.item.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item")
@ToString
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name =  "itemId", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private int itemPrice;

    private int orderCnt;

    private int likeCnt;

    private int reviewCnt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @LastModifiedDate // 엔티티 업데이트 시 자동으로 현재 시간을 기록
    private LocalDateTime updateAt;

    @Column(nullable = false)
    private String itemImg;

    private String itemInfImg;

    private int itemWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(nullable = false)
    private int count;


    @Builder
    public Item(String itemName, Long categoryId, int itemPrice, String itemImg, ItemStatus status, int count,
                int orderCnt, int likeCnt, int reviewCnt, int itemWeight, String itemInfImg) {
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.itemPrice = itemPrice;
        this.orderCnt = orderCnt;
        this.likeCnt = likeCnt;
        this.reviewCnt = reviewCnt;
        this.itemImg = itemImg;
        this.itemInfImg = itemInfImg;
        this.itemWeight = itemWeight;
        this.status = status;
        this.count = count;
    }

}
