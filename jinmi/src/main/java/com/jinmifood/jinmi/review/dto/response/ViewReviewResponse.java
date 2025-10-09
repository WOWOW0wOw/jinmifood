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

    private String content;
    private String image;


    public ViewReviewResponse(Review review) {
        this.content = review.getContent();
        this.image = review.getImage();
    }

}
