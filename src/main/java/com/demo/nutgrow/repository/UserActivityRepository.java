package com.demo.nutgrow.repository;

import com.demo.nutgrow.model.UserActivity;
import com.demo.nutgrow.model.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    long countByActivityType(ActivityType activityType);
}
