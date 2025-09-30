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

    @Transactional
    public void removeCart(Long userId, Long itemId) {

        ItemCart cart = itemCartRepository.findByItemIdAndUserId(itemId, userId);
        if(cart == null) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }
        itemCartRepository.delete(cart);

    }

    @Transactional
    public void removeAllCart(Long userId) {
        List<ItemCart> items = itemCartRepository.findAllByUserId(userId);
        itemCartRepository.deleteAllInBatch(items);
    }

    @Transactional
    public void updateOption(Long userId, Long cartId, String option) {
        ItemCart cart = itemCartRepository.findByItemIdAndUserId(cartId, userId);
        cart.setItemOption(option);
        itemCartRepository.save(cart);
    }


    @Transactional
    public void updateQuantity(Long userId, Long cartId, int qty) {
        if(qty < 1) {
            throw new CustomException(ErrorException.QTY_NOTZERO);
        }else if(qty > 100) {
            throw new CustomException(ErrorException.QTY_FULL);
        }
        //재고 검증 나중에 해야함



        ItemCart cart = itemCartRepository.findByItemIdAndUserId(cartId, userId);
        cart.setTotalCnt(qty);
        cart.setTotalPrice(qty * cart.getPrice());
        itemCartRepository.save(cart);
    }
}
