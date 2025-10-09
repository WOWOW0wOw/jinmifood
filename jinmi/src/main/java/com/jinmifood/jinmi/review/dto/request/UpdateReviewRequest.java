package com.jinmifood.jinmi.review.dto.request;

import com.jinmifood.jinmi.review.domain.Review;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UpdateReviewRequest {

    private String content;
    private String image;

    public Review toEntity() {
        return Review.builder()
                .content(this.content)
                .image(this.image)
                .build();
    }

}
