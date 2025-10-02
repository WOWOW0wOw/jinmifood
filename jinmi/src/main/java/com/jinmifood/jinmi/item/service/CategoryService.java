package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Category;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddCategoryRequest;
import com.jinmifood.jinmi.item.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category addCategory(AddCategoryRequest addCategory) {

        List<Category> categories = categoryRepository.findAllByCategoryName(addCategory.getCategoryName());

        log.info("categories: {}", categories);
        if(!categories.isEmpty()) { // 카테고리 이름 중복체크
            throw new CustomException(ErrorException.DUPLICATE_CATEGORY_NAME);
        }

        Category category = addCategory.toEntitiy();
        log.info("category : {}", category);
        categoryRepository.save(category);
        return category;
    }

}
