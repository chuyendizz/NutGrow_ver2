package com.demo.nutgrow.controller.admin;

import com.demo.nutgrow.model.User;
import com.demo.nutgrow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String getAllEmployees(Model model) {
        List<User> employees = userService.getAll();
        model.addAttribute("users", employees);
        return "admin/user/view";
    }

    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user/add";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User User,
                           Model model) {

        boolean isSaved = userService.saveNewUser(User);
        if (!isSaved) {
            model.addAttribute("emailError", "Email đã tồn tại rồi!");
            return "admin/user/add";
        }

        return "redirect:/admin/user?add=true";
    }


    @GetMapping("/update-status/{id}/{newStatus}")
    public String toggle(@PathVariable("id") Integer id,
                                       @PathVariable("newStatus") Integer newStatus) {
        boolean isUpdated = userService.toggleStatus(id, newStatus);
        if (isUpdated) {
            return "redirect:/admin/user?updateStatus=true";
        }
        return "redirect:/admin/user";
    }
}
