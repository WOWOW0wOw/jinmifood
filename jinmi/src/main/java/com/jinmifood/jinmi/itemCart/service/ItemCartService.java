package com.jinmifood.jinmi.itemCart.service;


import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.itemCart.domain.ItemCart;
import com.jinmifood.jinmi.itemCart.dto.request.AddItemCartReqest;
import com.jinmifood.jinmi.itemCart.dto.response.ViewItemCartResponse;
import com.jinmifood.jinmi.itemCart.repository.ItemCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemCartService {

    private final ItemCartRepository itemCartRepository;

    public List<ViewItemCartResponse> list(Long userId) {
        List<ItemCart> cartList = itemCartRepository.findAllByUserId(userId);
        return cartList.stream()
                .map(itemCart -> new ViewItemCartResponse(itemCart))
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemCart addItemCart(AddItemCartReqest addCart) {
        ItemCart cart = addCart.toEntity();
        if(itemCartRepository.existsByItemIdAndUserId(cart.getItemId(), cart.getUserId())) {
            throw new CustomException(ErrorException.DUPLICATE_CART);
        }
        if(itemCartRepository.countByUserId(addCart.getUserId()) < 10){
            itemCartRepository.save(cart);
        }else {
            throw new CustomException(ErrorException.FULL_CART);
        }
        return cart;
    }


}
