package com.demo.nutgrow.service;

import com.demo.nutgrow.model.User;
import com.demo.nutgrow.model.UserActivity;
import com.demo.nutgrow.model.enums.ActivityType;
import com.demo.nutgrow.repository.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ActivityService {

    @Autowired
    private UserActivityRepository activityRepository;

    public void logActivity(User user, ActivityType type) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setActivityType(type);
        activityRepository.save(activity);
    }

    public Map<ActivityType, Long> getActivityStats() {
        Map<ActivityType, Long> stats = new HashMap<>();
        for (ActivityType type : ActivityType.values()) {
            stats.put(type, activityRepository.countByActivityType(type));
        }
        return stats;
    }
}
