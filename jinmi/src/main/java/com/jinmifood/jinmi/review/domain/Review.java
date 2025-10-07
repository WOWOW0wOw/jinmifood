package com.jinmifood.jinmi.review.domain;


import com.jinmifood.jinmi.item.domain.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "reviews")
@ToString()
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewId", nullable = false)
    private Long reviewId;

    @Column(name = "itemId", nullable = false)
    private Long itemId;

    @Column(name = "authorId", nullable = false)
    private Long authorId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "image")
    private String image;

    @Column(name = "createdAt",nullable = false)
    private LocalDateTime createAt;

    @Column(name = "updatedAt")
    private LocalDateTime updateAt;


    @PrePersist
    public void prePersist() {
        this.createAt = this.createAt == null ? LocalDateTime.now() : this.createAt;
        this.updateAt = this.updateAt == null ? LocalDateTime.now() : this.updateAt;
    }




}
