package com.jinmifood.jinmi.item.repository;

import com.jinmifood.jinmi.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {


    List<Item> findAllByItemId(Long itemId);

    List<Item> findAllByItemName(String name);

    Item findItemByItemId(Long itemId);
}
