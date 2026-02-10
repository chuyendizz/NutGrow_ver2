package com.demo.nutgrow.service;

import com.demo.nutgrow.model.UpgradeOrder;
import com.demo.nutgrow.model.User;
import com.demo.nutgrow.model.enums.AccountTier;
import com.demo.nutgrow.model.enums.OrderStatus;
import com.demo.nutgrow.repository.UpgradeOrderRepository;
import com.demo.nutgrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private UpgradeOrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UpgradeOrder createOrder(User user, AccountTier packageType, BigDecimal amount, String proofImage) {
        UpgradeOrder order = new UpgradeOrder();
        order.setUser(user);
        order.setPackageType(packageType);
        order.setAmount(amount);
        order.setPaymentProofImage(proofImage);
        order.setOrderStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public List<UpgradeOrder> getPendingOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.PENDING);
    }

    public List<UpgradeOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<UpgradeOrder> getRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void approveOrder(Long orderId) {
        UpgradeOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order already processed");
        }

        order.setOrderStatus(OrderStatus.APPROVED);
        order.setProcessedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Upgrade user
        User user = order.getUser();
        AccountTier oldTier = user.getAccountTier();
        user.setAccountTier(order.getPackageType());

        // Extend expiry - logic can be improved to add to existing expiry if valid
        LocalDateTime newExpiry = LocalDateTime.now().plusMonths(1);
        if (user.getSubscriptionExpiry() != null && user.getSubscriptionExpiry().isAfter(LocalDateTime.now())) {
            newExpiry = user.getSubscriptionExpiry().plusMonths(1);
        }
        user.setSubscriptionExpiry(newExpiry);

        userRepository.save(user);

        // Log confirmation
        System.out.println("✅ ORDER APPROVED - User: " + user.getEmail() +
                " | Old Tier: " + oldTier +
                " | New Tier: " + user.getAccountTier() +
                " | Expiry: " + newExpiry);
    }

    @Transactional
    public void rejectOrder(Long orderId, String reason) {
        UpgradeOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order already processed");
        }

        order.setOrderStatus(OrderStatus.REJECTED);
        order.setProcessedAt(LocalDateTime.now());
        order.setAdminNote(reason);
        orderRepository.save(order);
    }

    public UpgradeOrder getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
}
