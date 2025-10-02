package com.jinmifood.jinmi.item.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.item.domain.Category;
import com.jinmifood.jinmi.item.dto.request.AddCategoryRequest;
import com.jinmifood.jinmi.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/add")
    public StatusResponseDTO addCategory(@RequestBody AddCategoryRequest addCategory) {
        Category category = categoryService.addCategory(addCategory);
       return StatusResponseDTO.ok(category);
    }

}
