package com.jinmifood.jinmi.review.dto.response;

import com.jinmifood.jinmi.review.domain.Review;
import com.jinmifood.jinmi.user.domain.User;
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
    private String userNickname;
    private String content;
    private String image;
    private LocalDateTime createdAt;

    public ViewReviewResponse(Review review, User user) {
        this.reviewId = review.getReviewId();
        this.userId = review.getUserId();
        this.userNickname = user.getDisplayName(); // User 객체에서 닉네임 가져오기
        this.content = review.getContent();
        this.image = review.getImage();
        this.createdAt = review.getCreateAt();
    }
}