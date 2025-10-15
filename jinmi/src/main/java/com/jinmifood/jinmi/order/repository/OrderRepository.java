package com.jinmifood.jinmi.order.repository;

import com.jinmifood.jinmi.order.domain.Orders;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    void deleteByIdAndUserId(Long orderId, Long userId);

    List<Orders> findAllByUserIdOrderByIdDesc(Long userId, Limit of);

    List<Orders> findAllByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long offset, Limit of);
}
