package com.jinmifood.jinmi.item.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @JsonBackReference // 역방향 참조: 직렬화에서 제외하여 무한 루프 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId")
    private Item item;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType; // 'MAIN' 또는 'INFO'

    // 연관관계 편의 메서드
    public void setItem(Item item) {
        this.item = item;
    }

    public enum ImageType {
        MAIN, // 대표 이미지
        INFO  // 상품 정보 이미지
    }
}
