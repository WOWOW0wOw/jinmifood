package com.jinmifood.jinmi.itemCart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;

public interface itemCartRepository extends JpaRepository<ItemCart, Long>{
}
