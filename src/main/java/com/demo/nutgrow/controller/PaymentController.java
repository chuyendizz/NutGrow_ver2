package com.demo.nutgrow.controller;

import com.demo.nutgrow.model.User;
import com.demo.nutgrow.model.enums.AccountTier;
import com.demo.nutgrow.service.OrderService;
import com.demo.nutgrow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/upgrade")
    public String showUpgradePage(@RequestParam("package") String packageType, Model model) {
        model.addAttribute("packageType", packageType);
        if ("PRO".equalsIgnoreCase(packageType)) {
            model.addAttribute("amount", "59000");
            model.addAttribute("packageName", "Pro Package");
        } else if ("PREMIUM".equalsIgnoreCase(packageType)) {
            model.addAttribute("amount", "99000");
            model.addAttribute("packageName", "Premium Package");
        }
        return "user/payment-confirm"; 
    }

    @PostMapping("/confirm")
    public String confirmPayment(@RequestParam("package") String packageType,
            @RequestParam("paymentProof") String paymentProof, 
            Authentication authentication) {

        String email = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        }

        if (email == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AccountTier tier = AccountTier.valueOf(packageType.toUpperCase());
        BigDecimal amount = "PRO".equalsIgnoreCase(packageType) ? new BigDecimal("59000") : new BigDecimal("99000");

        orderService.createOrder(user, tier, amount, paymentProof);

        return "redirect:/new-dashboard?paymentSuccess=true";
    }
}
