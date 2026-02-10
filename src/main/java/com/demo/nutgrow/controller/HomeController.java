// package com.demo.nutgrow.controller;

// import java.util.Collection;

// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;

// import jakarta.servlet.http.HttpServletRequest;

// @Controller
// public class HomeController {

//     @GetMapping("/")
//     private String indexHome(Model model, HttpServletRequest request) {
//         String username = SecurityContextHolder.getContext().getAuthentication().getName();
//         Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
//                 .getAuthorities();

//         model.addAttribute("currentUri", request.getRequestURI());

//         if (authorities.stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
//             model.addAttribute("email", username);
//             return "redirect:/admin/dashboard";
//         } else {
//             return "index";
//         }
//     }

//     @GetMapping("/home")
//     public String homePage(Model model, HttpServletRequest request) {
//         return indexHome(model, request);
//     }

//     @GetMapping("/index")
//     public String index(Model model, HttpServletRequest request) {
//         return indexHome(model, request);
//     }

//     @GetMapping("/analyze")
//     private String test() {
//         return "analyze";
//     }

//     @GetMapping("/pricing")
//     private String pricing() {
//         return "pricing";
//     }
// }


package com.demo.nutgrow.controller;

import com.demo.nutgrow.model.Document;
import com.demo.nutgrow.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class HomeController {

    // --- KHAI BÁO SERVICE (SỬA LỖI documentService cannot be resolved) ---
    @Autowired
    private DocumentService documentService;

    // --- 1. TRANG CHỦ (LANDING PAGE) ---
    @GetMapping("/")
    public String indexHome(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Logic: Đã đăng nhập -> Vào Dashboard. Chưa đăng nhập -> Xem Landing Page.
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            
            // Check quyền Admin
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                return "redirect:/admin/dashboard"; 
            } 
            
            // User thường -> Vào Dashboard mới
            return "redirect:/dashboard"; 
        }

        // Trả về trang giới thiệu (Landing Page)
        return "new-landing"; 
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpServletRequest request) {
        return indexHome(model, request);
    }

    // --- 2. TRANG BÀN HỌC (DASHBOARD MỚI) ---
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = "Bạn"; 

        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName(); 
        }
        model.addAttribute("username", username);
        
        return "new-dashboard"; 
    }

    // --- 3. TRANG THƯ VIỆN (SỬA LỖI List và Document) ---
    @GetMapping("/library")
    public String showLibrary(Model model) {
        // Lấy danh sách tất cả tài liệu đã lưu
        List<Document> documents = documentService.getAllDocuments();
        
        // Đẩy danh sách sang giao diện new-library.html
        model.addAttribute("documents", documents);
        
        return "new-library";
    }

    // --- CÁC TRANG PHỤ KHÁC ---
    @GetMapping("/pricing")
    public String pricing() { return "pricing"; }

    @GetMapping("/analyze")
    public String test() { return "analyze"; }
}