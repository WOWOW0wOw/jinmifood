package com.jinmifood.jinmi.item.dto.request;

import com.jinmifood.jinmi.item.domain.Category;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AddCategoryRequest {

    private String categoryName;


    public Category toEntitiy(){
        return Category.builder()
                .categoryName(this.categoryName)
                .build();
    }

}
