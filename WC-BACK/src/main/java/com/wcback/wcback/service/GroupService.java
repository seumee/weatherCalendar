package com.wcback.wcback.service;

import com.wcback.wcback.data.entity.Group;
import com.wcback.wcback.data.entity.Schedule;
import com.wcback.wcback.data.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class GroupService {
    GroupRepository groupRepository;
    ScheduleService scheduleService;

    // 멤버마다 그룹에 넣기
    public List<Group> loopMembers(String groupName, String[] members, String leaderName) {
        for (String member : members) {createGroup(member,groupName, leaderName);}
        return findById(groupName);
    }

    // 멤버를 그룹에 넣기
    public void createGroup(String member,String groupName, String leaderName) {
        Group group = new Group();
        group.setGroupid(groupName);
        group.setEmail(member);
        if (member.equals(leaderName)) {
            group.setLeader(true);
        }
        groupRepository.save(group);
    }
    
    // 그룹Id로 그룹 찾기
    public List<Group> findById(String groupName) {
        return groupRepository.findByGroupid(groupName);
    }

    // 그룹Id 중복체크
    public boolean checkGroup(String groupName) {
        return groupRepository.existsByGroupid(groupName);
    }

    // 그룹 삭제
    public void deleteById(String groupName) {
        groupRepository.deleteByGroupid(groupName);
    }

    // 그룹탈퇴
    public void groupOut(String groupName, String email) {
        groupRepository.deleteByGroupidAndEmail(groupName,email);
    }

    // 사용자를 포함하는 그룹 찾기
    public List<String> findGroupsContainUser(String email) {
        List<Group> groups = groupRepository.findGroupByEmail(email);
        List<String> groupids = new ArrayList<>();
        for (Group group : groups) {
            groupids.add(group.getGroupid());
        }
        return groupids;
    }

    // 사용자를 포함하는 그룹의 일정들 찾기
    public HashMap<String,List<Schedule>> findGroupSchedule(String email) {
        HashMap<String, List<Schedule>> groupSchedule = new HashMap<>();
        List<String> groupids = findGroupsContainUser(email);
        for (String groupid : groupids) {
            groupSchedule.put(groupid, scheduleService.getAllSchedules(groupid));
        }
        return groupSchedule;
    }
}

