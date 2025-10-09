package com.jinmifood.jinmi.review.domain;


import com.jinmifood.jinmi.item.domain.ItemStatus;
import com.jinmifood.jinmi.review.dto.request.AddReviewRequest;
import com.jinmifood.jinmi.review.dto.request.UpdateReviewRequest;
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

    public void updateReviewDetails(UpdateReviewRequest request){
        if(request.getContent() != null){
            this.content = request.getContent();
        }
        if(request.getImage() != null){
            this.image = request.getImage();
        }
        this.updateAt = LocalDateTime.now();
    }



}
