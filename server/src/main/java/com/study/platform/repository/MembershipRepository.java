package com.study.platform.repository;

import com.study.platform.domain.Group;
import com.study.platform.domain.Membership;
import com.study.platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByGroup(Group group);
    List<Membership> findByUser(User user);
    Optional<Membership> findByUserAndGroup(User user, Group group);
    boolean existsByUserAndGroup(User user, Group group);
    void deleteByUserAndGroup(User user, Group group);
}
