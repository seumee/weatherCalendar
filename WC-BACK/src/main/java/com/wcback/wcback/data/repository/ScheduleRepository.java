package com.wcback.wcback.data.repository;

import com.wcback.wcback.data.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ScheduleRepository extends JpaRepository<Schedule,Object> {
    List<Schedule> findByGroupID(String groupID);
    List<Schedule> findByEmail(String email);
    Schedule findByNum(int num);
    void deleteScheduleByNum(int num);
}