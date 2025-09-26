package com.jinmifood.jinmi.Item.repository;

import com.jinmifood.jinmi.Item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllbyitemId(Long itemId);

}
