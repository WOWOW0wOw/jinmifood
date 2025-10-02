package com.jinmifood.jinmi.order.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.order.domain.Order;
import com.jinmifood.jinmi.order.dto.request.AddOrderReqest;
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

    @GetMapping("/list")
    public StatusResponseDTO list(@RequestParam Long userId) {
        List<ViewOrderResponse> list = orderService.list(userId);
        return StatusResponseDTO.ok(list);
    }

    @PostMapping("/add")
    public StatusResponseDTO add(@RequestBody AddOrderReqest addOrderReqest){
        Order order = orderService.addOrder(addOrderReqest);
        return StatusResponseDTO.ok(order);
    }
}
