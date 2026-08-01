package com.karaboGrace.catalog.repository;

import com.karaboGrace.catalog.entity.Order;
import com.karaboGrace.catalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}