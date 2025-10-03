package com.jinmifood.jinmi.item.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.item.domain.Category;
import com.jinmifood.jinmi.item.dto.request.AddCategoryRequest;
import com.jinmifood.jinmi.item.dto.response.ViewCategoryResponse;
import com.jinmifood.jinmi.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/remove")
    public StatusResponseDTO removeCategory(@RequestParam Long categoryId) {
        categoryService.removeCategory(categoryId);
        return StatusResponseDTO.ok("삭제 완료");
    }

    @PostMapping("/removeAll")
    public StatusResponseDTO removeAllCategory() {
        categoryService.removeAllCategory();
        return StatusResponseDTO.ok("전체 삭제 완료");
    }

    @PostMapping("/update")
    public StatusResponseDTO updateCategory(@RequestParam Long categoryId, @RequestParam String categoryName) {
        categoryService.updateCategory(categoryId,categoryName);
        return StatusResponseDTO.ok("수정 완료");
    }

    @GetMapping("/list")
    public StatusResponseDTO viewCategoryList() {
        List<ViewCategoryResponse> List = categoryService.getCategoryList();
        return StatusResponseDTO.ok(List);
    }

}
