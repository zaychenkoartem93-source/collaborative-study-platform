package com.study.platform.service;

import com.study.platform.domain.Group;
import com.study.platform.domain.Membership;
import com.study.platform.domain.User;
import com.study.platform.repository.GroupRepository;
import com.study.platform.repository.MembershipRepository;
import com.study.platform.websocket.NotificationService; // Исправь пакет если другой
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groups;
    private final MembershipRepository memberships;
    private final NotificationService notificationService;

    public GroupService(GroupRepository groups,
                        MembershipRepository memberships,
                        NotificationService notificationService) {
        this.groups = groups;
        this.memberships = memberships;
        this.notificationService = notificationService;
    }

    public Group createGroup(User creator, String name, String description) {
        Group g = new Group(name, description, creator);
        g = groups.save(g);

        Membership m = new Membership(creator, g, Membership.Role.ADMIN);
        memberships.save(m);
        return g;
    }

    public List<Group> myGroups(User user) {
        return groups.findGroupsByUserId(user.getUserId());
    }

    public boolean isMember(User user, Group group) {
        return memberships.existsByUserAndGroup(user, group);
    }

    public boolean addMember(Group group, User user) {
        if (memberships.existsByUserAndGroup(user, group)) {
            return false;
        }
        Membership membership = new Membership(user, group, Membership.Role.MEMBER);
        memberships.save(membership);

        // Используй sendNewMember! Передавай groupId, userId и userName
        notificationService.sendNewMember(group.getGroupId(), user.getUserId(), user.getName());

        return true;
    }
}
