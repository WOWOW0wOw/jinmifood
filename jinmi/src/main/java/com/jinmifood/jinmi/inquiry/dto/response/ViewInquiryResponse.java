package com.jinmifood.jinmi.inquiry.dto.response;

import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ViewInquiryResponse {

    private Long inquiryId;
    private Long itemId;
    private Long userId;
    private String title;
    private String content;
    private String image;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private boolean answer;
    private String answerContent;

    public ViewInquiryResponse(Inquiry inquiry) {
        this.inquiryId = inquiry.getInquiryId();
        this.itemId = inquiry.getItemId();
        this.userId = inquiry.getUserId();
        this.title = inquiry.getTitle();
        this.content = inquiry.getContent();
        this.image = inquiry.getImage();
        this.createAt = inquiry.getCreateAt();
        this.updateAt = inquiry.getUpdateAt();
        this.answer = inquiry.isAnswer();
        this.answerContent = inquiry.getAnswerContent();
    }


}
