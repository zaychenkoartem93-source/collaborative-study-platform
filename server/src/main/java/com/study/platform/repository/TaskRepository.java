package com.study.platform.repository;

import com.study.platform.domain.Group;
import com.study.platform.domain.Task;
import com.study.platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroup(Group group);
    List<Task> findByGroupOrderByCreatedAtDesc(Group group);
    List<Task> findByCreatedBy(User user);
    List<Task> findByGroupAndStatus(Group group, Task.Status status);
}
