package com.demo.nutgrow.repository;

import com.demo.nutgrow.model.UpgradeOrder;
import com.demo.nutgrow.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpgradeOrderRepository extends JpaRepository<UpgradeOrder, Long> {
    List<UpgradeOrder> findByOrderStatus(OrderStatus orderStatus);

    List<UpgradeOrder> findByUserId(Long userId);

    List<UpgradeOrder> findTop5ByOrderByCreatedAtDesc();
}
