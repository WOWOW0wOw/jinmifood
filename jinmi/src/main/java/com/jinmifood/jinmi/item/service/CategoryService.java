package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Category;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddCategoryRequest;
import com.jinmifood.jinmi.item.dto.response.ViewCategoryResponse;
import com.jinmifood.jinmi.item.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public void removeCategory(Long categoryId) {
        List<Category> category = categoryRepository.findAllByCategoryId(categoryId);
        log.info("category : {}", category);
        if(category.isEmpty()) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void removeAllCategory() {
        log.info("category : {}", categoryRepository.findAll());
        if(categoryRepository.findAll().isEmpty()) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        categoryRepository.deleteAll();
        log.info("category : {}", categoryRepository.findAll());

    }

    @Transactional
    public void updateCategory(Long categoryId, String categoryName) {

        Category category = categoryRepository.findCategoryByCategoryId(categoryId);
        log.info("수정 전 category : {}", category);
        if(category == null) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }

        category.setCategoryName(categoryName);
        log.info("수정 후 category : {}", category);
    }

    public List<ViewCategoryResponse> getCategoryList() {
        List<Category> categoryList = categoryRepository.findAll();
        log.info("categoryList: {}", categoryList);
        if(categoryList.isEmpty()) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        return  categoryList.stream()
                .map(category -> new ViewCategoryResponse(category.getCategoryId(), category.getCategoryName()))
                .collect(Collectors.toList());
    }

}
