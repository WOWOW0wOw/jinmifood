package com.jinmifood.jinmi.itemCart.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import com.jinmifood.jinmi.itemCart.dto.request.AddItemCartReqest;
import com.jinmifood.jinmi.itemCart.dto.response.ViewItemCartResponse;
import com.jinmifood.jinmi.itemCart.service.ItemCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/itemCart")
public class ItemCartController {

    private final ItemCartService itemCartService;

    @GetMapping({""})
    public StatusResponseDTO list(@AuthenticationPrincipal(expression = "id") Long userId) {
        log.info("userId = {}", userId);
        List<ViewItemCartResponse> list =  itemCartService.list(userId);
        log.info("list = {}", list);
        return  StatusResponseDTO.ok(list);
    }

    @PostMapping({"/add"})
    public StatusResponseDTO add(@RequestBody AddItemCartReqest addCart) {
        ItemCart cart = itemCartService.addItemCart(addCart);
        return StatusResponseDTO.ok(cart);
    }

    @PostMapping({"/remove/{cartId}"})
    public StatusResponseDTO remove(@AuthenticationPrincipal(expression = "id") Long userId, @PathVariable Long cartId){
        itemCartService.removeCart(userId, cartId);
        return StatusResponseDTO.ok("삭제 완료");
    }

    @PostMapping({"/removeAll"})
    public StatusResponseDTO removeAll(@AuthenticationPrincipal(expression = "id") Long userId){
        itemCartService.removeAllCart(userId);
        return StatusResponseDTO.ok("전체 삭제 완료");
    }

    @PostMapping("/update/{cartId}/{qty}")
    public StatusResponseDTO update(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long cartId,
            @PathVariable int qty
    ) {
        log.info("userId = {}, cartId = {}, qty = {}", userId, cartId, qty);
        itemCartService.updateQuantity(userId, cartId, qty);
        return StatusResponseDTO.ok("수량 수정 완료");
    }

    @PostMapping({"/update/option"})
    public StatusResponseDTO updateOption(@RequestParam Long userId, @RequestParam Long itemId, @RequestParam String option){
        itemCartService.updateOption(userId, itemId, option);
        return StatusResponseDTO.ok("옵션 수정 완료");
    }

}
