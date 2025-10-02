package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Category;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddItemRequest;
import com.jinmifood.jinmi.item.dto.response.ViewItemResponse;
import com.jinmifood.jinmi.item.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    public List<ViewItemResponse> list(Long itemId) {
        List<Item> itemList = itemRepository.findAllByItemId(itemId);
        return itemList.stream()
                .map(item -> new ViewItemResponse(item))
                .collect(Collectors.toList());
    }


    @Transactional
    public Item AddItem(AddItemRequest request) {
        List<Item> itemList = itemRepository.findAllByItemName(request.getItemName());
        log.info("아이템 이름 중복 검사 itemNameList: {}", itemList);
        if(!itemList.isEmpty()) { // 아이템이름 중복 검사
            throw new CustomException(ErrorException.DUPLICATE_ITEM_NAME);
        }

        Long categoryId = request.getCategoryId();
        List<Category> categories = categoryRepository.findAllByCategoryId(categoryId);
        log.info("카테고리 존재성 검사 categoryId: {}", categoryId);
        if(categories.isEmpty()) {
            // 요청한 카테고리id 존재 여부 검사
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        log.info("아이템 이름 중복 검사 완료");
        Item item = request.toEntity();
        log.info("아이템 저장 준비 Item: {}", item);
        itemRepository.save(item);
        log.info("아이템 저장 완료 Item: {}", item);
        return item;
    }

}
