package com.wcback.wcback.data.repository;

import com.wcback.wcback.data.entity.Group;
import com.wcback.wcback.data.entity.GroupPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, GroupPK> {
    boolean existsByGroupid(String groupid);
    void deleteByGroupid(String groupid);
    List<Group> findByGroupid(String groupName);
    void deleteByGroupidAndEmail(String groupName, String email);
    List<Group> findGroupByEmail(String email);
}

