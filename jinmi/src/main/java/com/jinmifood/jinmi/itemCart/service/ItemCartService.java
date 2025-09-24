package com.jinmifood.jinmi.itemCart.service;

import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import com.jinmifood.jinmi.itemCart.dto.response.ViewItemCartResponse;
import com.jinmifood.jinmi.itemCart.repository.ItemCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemCartService {

    private final ItemCartRepository itemCartRepository;

    public List<ViewItemCartResponse> list(Long userId) {
        List<ItemCart> cartList = itemCartRepository.findAllbyuserId(userId);
        return cartList.stream()
                .map(itemCart -> new ViewItemCartResponse(itemCart))
                .collect(Collectors.toList());
    }
}
