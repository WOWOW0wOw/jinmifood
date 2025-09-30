package com.jinmifood.jinmi.item.controller;

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

    @GetMapping({""})
    public StatusResponseDTO List(@PathVariable Long ItemId) {
        List<ViewItemResponse> list = itemService.list(ItemId);
        return StatusResponseDTO.ok(list);
    }

    @PostMapping("create")
    public StatusResponseDTO AddItem(@RequestBody AddItemRequest request) {
        return  null;
    }

}
