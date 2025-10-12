package com.jinmifood.jinmi.inquiry.dto.request;

import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UpdateInquiryRequest {

    private String content;
    private String title;
    private String image;

    public Inquiry toEntity() {
        return Inquiry.builder()
                .content(this.content)
                .title(this.title)
                .image(this.image)
                .build();
    }

}
