package com.jinmifood.jinmi.order.service;

import com.jinmifood.jinmi.order.domain.Order;
import com.jinmifood.jinmi.order.dto.request.AddOrderReqest;
import com.jinmifood.jinmi.order.dto.response.ViewOrderResponse;
import com.jinmifood.jinmi.order.repository.orderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class orderService {

    private final orderRepository orderRepository;

    public String createOrderCode(){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String randomString = "";
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            randomString += alphabet.charAt(random.nextInt(alphabet.length()));
            randomString += numbers.charAt(random.nextInt(numbers.length()));
        }
        String code = randomString + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "-";
        log.info("code : {}", code);
        return code;
    }

    public List<ViewOrderResponse> list(Long userId) {
        List<Order> orderList = orderRepository.findAllByUserId(userId);
        return orderList.stream()
                .map(order -> new ViewOrderResponse(order))
                .collect(Collectors.toList());
    }

    @Transactional
    public Order addOrder(AddOrderReqest order) {
        Order addOrder = order.toEntity();
        addOrder.setOrderCode(createOrderCode());
        return orderRepository.save(addOrder);
    }
}
