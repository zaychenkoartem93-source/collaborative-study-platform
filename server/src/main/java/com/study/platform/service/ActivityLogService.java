package com.study.platform.service;

import com.study.platform.domain.ActivityLog;
import com.study.platform.domain.User;
import com.study.platform.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {
    private final ActivityLogRepository logs;

    public ActivityLogService(ActivityLogRepository logs) {
        this.logs = logs;
    }

    public void log(User user, String action, String details) {
        logs.save(new ActivityLog(user, action, details));
    }
}
