package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.response.ViewItemResponse;
import com.jinmifood.jinmi.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public List<ViewItemResponse> list(Long itemId) {
        List<Item> itemList = itemRepository.findAllByItemId(itemId);
        return itemList.stream()
                .map(item -> new ViewItemResponse(item))
                .collect(Collectors.toList());
    }

}
