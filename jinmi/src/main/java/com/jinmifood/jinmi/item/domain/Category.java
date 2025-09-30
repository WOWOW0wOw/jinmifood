package com.jinmifood.jinmi.item.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category")
@ToString
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoryId", nullable = false)
    private Long categoryId;

    @Column(name = "categoryName", nullable = false, length = 16)
    private String categoryName;

    @Column(name = "itemID", nullable = false)
    private Long itemId;

    @Builder
    public Category(Long categoryId, Long itemId, String categoryName) {
        this.categoryId = categoryId;
        this.itemId = itemId;
        this.categoryName = categoryName;
    }

}
