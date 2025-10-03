package com.jinmifood.jinmi.order.service;

import com.jinmifood.jinmi.order.domain.Orders;
import com.jinmifood.jinmi.order.dto.request.AddOrderRequest;
import com.jinmifood.jinmi.order.dto.response.ViewOrderResponse;
import com.jinmifood.jinmi.order.repository.orderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
        String code = randomString + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        log.info("code : {}", code);
        return code;
    }

    public List<ViewOrderResponse> list(Long userId, Long offset) {
        List<Orders> orderList = offset == null ? orderRepository.findAllByUserIdOrderByIdDesc(userId, Limit.of(10))
                : orderRepository.findAllByUserIdAndIdLessThanOrderByIdDesc(userId, offset, Limit.of(10));
        return orderList.stream()
                .map(order -> new ViewOrderResponse(order))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Orders> addOrder(List<AddOrderRequest> order) {
        List<Orders> addOrder = new ArrayList<>();
        for (AddOrderRequest addorderRequest : order) {
            addOrder.add(addorderRequest.toEntity());
        }
        String code = createOrderCode();
        for (Orders CreateOrder : addOrder) {
            CreateOrder.setOrderCode(code);
        }
        return orderRepository.saveAll(addOrder);
    }

    @Transactional
    public void removeOrder(Long userId, Long orderId) {
        orderRepository.deleteByIdAndUserId(orderId, userId);
    }

    // 배송상태 나중에 추가

}
