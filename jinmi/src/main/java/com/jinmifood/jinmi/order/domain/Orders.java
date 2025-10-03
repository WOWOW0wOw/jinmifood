package com.jinmifood.jinmi.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@ToString()
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId", nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String itemOption;

    @Column(nullable = false)
    private String orderCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String orderTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private String itemImg;

    @Builder
    public Orders(Long itemId, Long userId, String itemName, String itemOption, String orderCode, OrderStatus orderStatus, String orderTime, PaymentType paymentType, Integer price, Integer qty, String itemImg) {
        this.itemId = itemId;
        this.userId = userId;
        this.itemName = itemName;
        this.itemOption = itemOption;
        this.orderCode = orderCode;
        this.orderStatus = orderStatus;
        this.orderTime = orderTime;
        this.paymentType = paymentType;
        this.price = price;
        this.qty = qty;
        this.totalPrice = price * qty;
        this.itemImg = itemImg;

    }
}