package com.jinmifood.jinmi.itemCart.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "itemCart")
@ToString()
public class ItemCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartId", nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String itemName;

    @Column
    private String itemOption;

    @Column(nullable = false)
    private Integer totalCnt;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer totalPrice;

    @Builder
    public ItemCart(Long itemId, Long userId,String itemName, String itemOption, Integer totalCnt, Integer price) {
        this.itemId = itemId;
        this.userId = userId;
        this.itemName = itemName;
        this.itemOption = itemOption;
        this.totalCnt = totalCnt;
        this.price = price;
        this.totalPrice = totalCnt * price;
    }

}
