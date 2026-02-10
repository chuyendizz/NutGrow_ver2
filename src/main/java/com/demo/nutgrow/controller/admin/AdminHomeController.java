package com.demo.nutgrow.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

        @Autowired
        private com.demo.nutgrow.service.UserService userService;

        @Autowired
        private com.demo.nutgrow.service.OrderService orderService;

        @GetMapping("/dashboard")
        public String home(Model model, HttpSession session) {
                // Stats
                long totalUsers = userService.countUsers();
                model.addAttribute("totalUsers", totalUsers);

                // Revenue (Mock or calculation - implementing simple sum for now)
                // In a real app, this should be a service method
                List<com.demo.nutgrow.model.UpgradeOrder> allOrders = orderService.getAllOrders();
                java.math.BigDecimal totalRevenue = allOrders.stream()
                                .filter(o -> o.getOrderStatus() == com.demo.nutgrow.model.enums.OrderStatus.APPROVED)
                                .map(com.demo.nutgrow.model.UpgradeOrder::getAmount)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                model.addAttribute("totalRevenue", totalRevenue);

                // Pending Orders Count
                long pendingCount = allOrders.stream()
                                .filter(o -> o.getOrderStatus() == com.demo.nutgrow.model.enums.OrderStatus.PENDING)
                                .count();
                model.addAttribute("pendingCount", pendingCount);

                // Recent Orders
                model.addAttribute("recentOrders", orderService.getRecentOrders());

                return "admin/dashboard";
        }
}