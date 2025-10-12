package com.jinmifood.jinmi.inquiry.domain;


import com.jinmifood.jinmi.inquiry.dto.request.UpdateInquiryRequest;
import jakarta.persistence.*;
import lombok.*;
import org.w3c.dom.Text;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "inquirys")
@ToString()
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiryId",nullable = false)
    private Long inquiryId;

    @Column(name = "itemId",nullable = false)
    private Long itemId;

    @Column(name = "userId",nullable = false)
    private Long userId;

    @Column(name = "title",nullable = false)
    private String title;

    @Lob
    @Column(name = "content",nullable = false)
    private String content;

    @Column(name = "image")
    private String image;

    @Column(name = "createAt",nullable = false)
    private LocalDateTime createAt;

    @Column(name = "updateAt")
    private LocalDateTime updateAt;

    @Column(name = "answer")
    private boolean answer;

    @Lob
    @Column(name = "answerContent")
    private String answerContent;


    @PrePersist
    public void prePersist() {
        this.createAt = this.createAt == null ? LocalDateTime.now() : this.createAt;
        this.updateAt = this.updateAt == null ? LocalDateTime.now() : this.updateAt;
    }

    public void updateAnswer() {
        this.answer = true;
    }

    public void updateAnswerContent(String answerContent) {
        this.answerContent = this.answerContent == null ? "" : answerContent;
        this.answer = true;
    }

    public void updateInquiryDetails(UpdateInquiryRequest request) {
        this.content = request.getContent();
        this.title = request.getTitle();
        this.image = request.getImage();
        this.updateAt = LocalDateTime.now();
    }


}
