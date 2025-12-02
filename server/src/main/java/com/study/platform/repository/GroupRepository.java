package com.study.platform.repository;

import com.study.platform.domain.Group;
import com.study.platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCreatedBy(User user);

    @Query("SELECT g FROM Group g JOIN Membership m ON g.groupId = m.group.groupId WHERE m.user.userId = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);
}
