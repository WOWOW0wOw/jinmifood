package com.jinmifood.jinmi.item.controller;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddItemRequest;
import com.jinmifood.jinmi.item.dto.request.UpdateItemRequest;
import com.jinmifood.jinmi.item.dto.response.ViewItemResponse;
import com.jinmifood.jinmi.item.service.ItemService;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;



    @PostMapping("/add")
    public StatusResponseDTO addItem(@RequestBody AddItemRequest addItem) {

        Item item = itemService.AddItem(addItem);
        return StatusResponseDTO.ok(item);

    }

    @PostMapping("/remove")
    public StatusResponseDTO removeItem(@RequestParam long itemId) {
        itemService.removeItem(itemId);
        return StatusResponseDTO.ok("삭제 완료");
    }

    @PostMapping("/removeAll")
    public StatusResponseDTO removeAllItems() {
        itemService.removeAllItem();
        return StatusResponseDTO.ok("전체 삭제 완료");
    }

    @PostMapping("/update/{itemId}")
    public StatusResponseDTO updateItem(@PathVariable Long itemId, @RequestBody UpdateItemRequest updateItem)   {

        Item updatedItem = itemService.updateItem(itemId, updateItem);

        return StatusResponseDTO.ok(updatedItem);
    }

    @GetMapping("/list")
    public StatusResponseDTO viewItemList() {
        List<ViewItemResponse> list = itemService.getItemList();
        return StatusResponseDTO.ok(list);
    }

    @GetMapping("/listByCategory")
    public StatusResponseDTO viewItemListByCategory(@RequestParam Long categoryId) {
        List<ViewItemResponse> list = itemService.getItemListByCategoryId(categoryId);
        return StatusResponseDTO.ok(list);
    }


}
