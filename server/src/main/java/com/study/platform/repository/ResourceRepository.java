package com.study.platform.repository;

import com.study.platform.domain.Group;
import com.study.platform.domain.ResourceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceItem, Long> {
    List<ResourceItem> findByGroup(Group group);
    List<ResourceItem> findByGroupOrderByUploadedAtDesc(Group group);
}
