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

    @Column(nullable = false)
    private String itemImg;

    private String itemInfImg;

    private int itemWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(nullable = false)
    private int count;

    @PrePersist
    public void prePersist() {
        this.createAt = this.createAt == null ? LocalDateTime.now() : this.createAt;
        this.updateAt = this.updateAt == null ? LocalDateTime.now() : this.updateAt;
        this.status = this.count == 0 ? ItemStatus.SOLDOUT : ItemStatus.SALE;
    }


}
