package com.demo.nutgrow.controller;

import com.demo.nutgrow.model.enums.ActivityType;
import com.demo.nutgrow.service.ActivityService;
import com.demo.nutgrow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @PostMapping("/log")
    public ResponseEntity<Void> logActivity(@RequestParam ActivityType type,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                activityService.logActivity(user, type);
            });
        }
        return ResponseEntity.ok().build();
    }
}
