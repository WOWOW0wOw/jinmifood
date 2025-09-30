package com.jinmifood.jinmi.itemCart.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import com.jinmifood.jinmi.itemCart.dto.request.AddItemCartReqest;
import com.jinmifood.jinmi.itemCart.dto.response.ViewItemCartResponse;
import com.jinmifood.jinmi.itemCart.service.ItemCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/itemCart")
public class ItemCartController {

    private final ItemCartService itemCartService;

    @GetMapping({"/list"})
    public StatusResponseDTO list(@RequestParam Long userId) {
        List<ViewItemCartResponse> list =  itemCartService.list(userId);
        return  StatusResponseDTO.ok(list);
    }

    @PostMapping({"/add"})
    public StatusResponseDTO add(@RequestBody AddItemCartReqest addCart) {

        ItemCart cart = itemCartService.addItemCart(addCart);

        return StatusResponseDTO.ok(cart);
    }

}
