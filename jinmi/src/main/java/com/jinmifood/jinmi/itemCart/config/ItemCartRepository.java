package com.jinmifood.jinmi.itemCart.config;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;

import java.util.List;


public interface ItemCartRepository extends JpaRepository<ItemCart, Long>{


    Long countByUserId(Long userId);

    List<ItemCart> findAllByUserId(Long userId);

    boolean existsByItemIdAndUserId(Long itemId, Long userId);

    ItemCart findByItemIdAndUserId(Long itemId, Long userId);

    ItemCart findByIdAndUserId(Long cartId, Long userId);
}


