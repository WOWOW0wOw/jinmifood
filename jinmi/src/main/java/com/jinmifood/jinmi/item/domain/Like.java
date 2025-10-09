package com.jinmifood.jinmi.item.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "likes")
@ToString()
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "itemId", nullable = false)
    private Long itemId;


}
