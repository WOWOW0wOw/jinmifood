package com.jinmifood.jinmi.review.dto.request;

import com.jinmifood.jinmi.review.domain.Review;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AddReviewRequest {

    private Long itemId;
    private Long authorId;
    private Long userId;
    private String content;
    private String image;

    public Review toEntity() {
        return Review.builder()
                .itemId(this.itemId)
                .authorId(this.authorId)
                .userId(this.userId)
                .content(this.content)
                .image(this.image)
                .build();
    }

}
