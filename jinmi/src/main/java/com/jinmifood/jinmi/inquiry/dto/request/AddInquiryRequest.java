package com.jinmifood.jinmi.inquiry.dto.request;

import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AddInquiryRequest {

    private Long itemId;
    private Long userId;
    private String content;
    private String title;
    private String image;

    public Inquiry toEntity() {
        return Inquiry.builder()
                .itemId(this.itemId)
                .userId(this.userId)
                .content(this.content)
                .title(this.title)
                .image(this.image)
                .build();
    }

}
