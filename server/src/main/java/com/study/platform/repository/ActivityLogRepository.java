package com.study.platform.repository;

import com.study.platform.domain.ActivityLog;
import com.study.platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserOrderByTimestampDesc(User user);

    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentActivity(@Param("since") Instant since);

    @Query("SELECT a FROM ActivityLog a WHERE a.user.userId IN " +
            "(SELECT m.user.userId FROM Membership m WHERE m.group.groupId = :groupId) " +
            "ORDER BY a.timestamp DESC")
    List<ActivityLog> findGroupActivity(@Param("groupId") Long groupId);
}
