package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Category;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddItemRequest;
import com.jinmifood.jinmi.item.dto.request.UpdateItemRequest;
import com.jinmifood.jinmi.item.dto.response.ViewItemResponse;
import com.jinmifood.jinmi.item.repository.CategoryRepository;
import com.jinmifood.jinmi.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Transactional
    public void removeItem(@RequestParam long itemId) {
        Item item = itemRepository.findItemByItemId(itemId);
        log.info("item: {}", item);
        if(item == null) {
            System.out.println("송명보형 바보");
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        itemRepository.delete(item);
    }

    @Transactional
    public void removeAllItem() {
        itemRepository.deleteAll();
    }


    @Transactional
    public Item updateItem(Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findItemByItemId(itemId);

        if(item == null) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }

        if (request.getItemName() != null && !request.getItemName().equals(item.getItemName())) {
            List<Item> existingItems = itemRepository.findAllByItemName(request.getItemName());
            if (!existingItems.isEmpty() && existingItems.stream().anyMatch(i -> !i.getItemId().equals(itemId))) {
                throw new CustomException(ErrorException.DUPLICATE_ITEM_NAME);
            }
        }

        item.updateItemDetails(request);

        log.info("아이템 수정 완료 item: {}", item);
        return item;
    }

    public List<ViewItemResponse> getItemList(){

        List<Item> itemList = itemRepository.findAll();
        log.info("ItemList: {}", itemList);
        if(itemList.isEmpty()) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }

        return itemList.stream()
                .map(ViewItemResponse::new)
                .collect(Collectors.toList());
    }

    public List<ViewItemResponse> getItemListByCategoryId(Long categoryId){
        List<Item> itemList = itemRepository.findAllByCategoryId(categoryId);
        log.info("ItemList: {}", itemList);
        if(itemList.isEmpty()) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        return itemList.stream()
                .map(ViewItemResponse::new)
                .collect(Collectors.toList());
    }

}
