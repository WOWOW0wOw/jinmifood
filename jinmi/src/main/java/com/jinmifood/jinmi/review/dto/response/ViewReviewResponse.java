package com.jinmifood.jinmi.review.dto.response;

import com.jinmifood.jinmi.review.domain.Review;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ViewReviewResponse {

    private Long reviewId;
    private Long userId;
    private Long authorId;
    private long itemId;
    private String content;
    private String image;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public ViewReviewResponse(Review review) {
        this.reviewId = review.getReviewId();
        this.userId = review.getUserId();
        this.authorId = review.getAuthorId();
        this.itemId = review.getItemId();
        this.content = review.getContent();
        this.image = review.getImage();
        this.createAt = review.getCreateAt();
        this.updateAt = review.getUpdateAt();
    }

}
