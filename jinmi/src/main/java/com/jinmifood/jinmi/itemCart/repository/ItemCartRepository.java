package com.jinmifood.jinmi.itemCart.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;

import java.util.List;


public interface ItemCartRepository extends JpaRepository<ItemCart, Long>{


    Long countByUserId(Long userId);

    List<ItemCart> findAllByUserId(Long userId);

    boolean existsByItemIdAndUserId(Long itemId, Long userId);
}


