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
    @Column(name = "itemCartId", nullable = false)
    private Long id;
}
