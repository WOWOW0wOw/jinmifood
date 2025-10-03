package com.jinmifood.jinmi.item.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ViewCategoryResponse {

    private Long categoryId;
    private String categoryName;

    public ViewCategoryResponse(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

}
