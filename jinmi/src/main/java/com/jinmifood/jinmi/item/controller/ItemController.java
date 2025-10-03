package com.jinmifood.jinmi.item.controller;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddItemRequest;
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

}
