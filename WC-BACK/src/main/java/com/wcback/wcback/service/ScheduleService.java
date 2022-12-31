package com.wcback.wcback.service;

import com.wcback.wcback.config.JwtProvider;
import com.wcback.wcback.data.entity.Alert;
import com.wcback.wcback.data.entity.Schedule;
import com.wcback.wcback.data.repository.AlertRepository;
import com.wcback.wcback.data.repository.ScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    JwtProvider jwtProvider;
    public int create(String ID, String scheduleName, Date date, Date endDate, String stringAddress) {
        Schedule schedule = new Schedule();
        if (isEmail(ID)) schedule.setEmail(ID);
        else schedule.setGroupID(ID);
        schedule.setScheduleName(scheduleName);
        schedule.setAppointment(date);
        schedule.setAppointmentDue(endDate);
        schedule.setAddress(stringAddress);
        scheduleRepository.save(schedule);
        scheduleRepository.flush();
        return schedule.getNum();
    }

    public List<Schedule> getAllSchedules(String ID) {
        if(isEmail(ID)) return scheduleRepository.findByEmail(ID);
        else return scheduleRepository.findByGroupID(ID);
    }

    public List<Integer> getNumOfSchedules(List<Schedule> schedules) {
        List<Integer> numList = new ArrayList<>();
        for (Schedule s : schedules) {
            numList.add(s.getNum());
        }
        return numList;
    }

    public Schedule getSchedule(int num) {
        return scheduleRepository.findByNum(num);
    }

    public void deleteSchedule(int num) {
        scheduleRepository.deleteScheduleByNum(num);
    }

    public void modifySchedule(int num, String scheduleName, Date date, String stringAddress) {
        Schedule schedule = scheduleRepository.findByNum(num);
        schedule.setScheduleName(scheduleName);
        schedule.setAppointment(date);
        schedule.setAddress(stringAddress);
        scheduleRepository.save(schedule);
    }

    public boolean isEmail(String ID) {
        return ID.contains("@");
    }


}
