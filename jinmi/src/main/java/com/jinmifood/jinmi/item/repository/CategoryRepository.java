package com.jinmifood.jinmi.item.repository;

import com.jinmifood.jinmi.item.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByCategoryName(String name);
    List<Category> findAllByCategoryId(Long id);
    Category findCategoryByCategoryId(Long id);

}
