package com.jinmifood.jinmi.order.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.order.domain.Orders;
import com.jinmifood.jinmi.order.dto.request.AddOrderRequest;
import com.jinmifood.jinmi.order.dto.response.ViewOrderResponse;
import com.jinmifood.jinmi.order.service.orderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class orderController {

    private final orderService orderService;

    @GetMapping({"/list", "/list/{offset}"})
    public StatusResponseDTO list(@RequestParam Long userId, Long offset) {
        List<ViewOrderResponse> list = orderService.list(userId, offset);
        return StatusResponseDTO.ok(list);
    }

    @PostMapping({"/add"})
    public StatusResponseDTO add(@RequestBody List<AddOrderRequest> addOrderRequest){
        List<Orders> order = orderService.addOrder(addOrderRequest);
        return StatusResponseDTO.ok(order);
    }

    @PostMapping({"/remove"})
    public StatusResponseDTO remove(@RequestParam Long userId, @RequestParam Long orderId){
        orderService.removeOrder(userId, orderId);
        return StatusResponseDTO.ok("삭제 완료");
    }
}
