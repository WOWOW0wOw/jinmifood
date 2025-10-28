package com.jinmifood.jinmi.item.controller;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.dto.request.AddItemRequest;
import com.jinmifood.jinmi.item.dto.request.UpdateItemRequest;
import com.jinmifood.jinmi.item.dto.response.ViewItemResponse;
import com.jinmifood.jinmi.item.service.ItemService;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;



    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StatusResponseDTO addItem(@RequestPart("addItem") AddItemRequest addItem,
                                     @RequestPart(value = "itemImgFile", required = false) MultipartFile itemImgFile,
                                     @RequestPart(value = "itemInfImgFile", required = false) MultipartFile itemInfImgFile
    ) throws IOException {
        System.out.println("컨트롤러부분");
        Item item = itemService.AddItem(addItem, itemImgFile, itemInfImgFile);
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

    @GetMapping("/itemDetail/{itemId}")
    public StatusResponseDTO viewItemDetail(@PathVariable Long itemId) {
        Item item = itemService.getItem(itemId);
        return StatusResponseDTO.ok(item);
    }


}
