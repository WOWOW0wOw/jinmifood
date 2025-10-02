package com.jinmifood.jinmi.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order")
@ToString()
public class order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId", nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String orderStatus;
}
