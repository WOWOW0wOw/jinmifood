package com.jinmifood.jinmi.item.dto.request;

import com.jinmifood.jinmi.item.domain.Like;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AddLikeRequest {

    private Long itemId;

    public Like toEntity(){
        return Like.builder()
                .itemId(this.itemId)
                .build();
    }

}
