package com.study.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "memberships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "group_id"})
})
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long membershipId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    public enum Role {
        MEMBER, ADMIN
    }

    public Membership() {}

    public Membership(User user, Group group, Role role) {
        this.user = user;
        this.group = group;
        this.role = role;
    }

    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
