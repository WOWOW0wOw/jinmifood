package com.jinmifood.jinmi.order.repository;

import com.jinmifood.jinmi.order.domain.Order;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface orderRepository extends JpaRepository<Order, Long> {

    void deleteByIdAndUserId(Long orderId, Long userId);

    List<Order> findAllByUserIdOrderByIdDesc(Long userId, Limit of);

    List<Order> findAllByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long offset, Limit of);
}
