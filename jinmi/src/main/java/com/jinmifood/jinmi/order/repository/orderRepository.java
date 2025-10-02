package com.jinmifood.jinmi.order.repository;

import com.jinmifood.jinmi.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface orderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);
}
