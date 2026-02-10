package com.demo.nutgrow.controller.admin;

import com.demo.nutgrow.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/order-list"; // You need to create this view
    }

    @PostMapping("/approve")
    public String approveOrder(@RequestParam("id") Long id) {
        orderService.approveOrder(id);
        return "redirect:/admin/orders";
    }

    @PostMapping("/reject")
    public String rejectOrder(@RequestParam("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        orderService.rejectOrder(id, reason);
        return "redirect:/admin/orders";
    }
}
