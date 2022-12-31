package com.wcback.wcback.controller;

import com.wcback.wcback.config.JwtProvider;
import com.wcback.wcback.data.dto.Group.GroupDto;
import com.wcback.wcback.data.dto.Schedule.ScheduleDto;
import com.wcback.wcback.data.entity.Alert;
import com.wcback.wcback.data.entity.Schedule;
import com.wcback.wcback.exception.user.AlreadyExistException;
import com.wcback.wcback.service.GroupService;
import com.wcback.wcback.service.OAuthService;
import com.wcback.wcback.service.ScheduleService;
import com.wcback.wcback.service.WideService;
import io.swagger.models.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/calander")
@AllArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final GroupController groupController;
    private final GroupService groupService;
    private final WideService wideService;
    private final OAuthService oAuthService;
    JwtProvider jwtProvider;


    //ID로(Email / GroupID 무관) 해당 ID의 전체 일정 조회
    @Transactional
    @PostMapping("/getAllSchedule")
    public ResponseEntity<Object> getAllSchedule(@RequestBody String ID) {
        return ResponseEntity.ok().body(scheduleService.getAllSchedules(ID));
    }

    //일정 num으로 일정 상세 조회
    @Transactional
    @GetMapping("/getSchedule")
    public ResponseEntity<Object> getSchedule(@RequestParam int scheduleNum) {
        return ResponseEntity.ok().body(scheduleService.getSchedule(scheduleNum));
    }

    //일정 생성
    @PostMapping("/createSchedule")
    public ResponseEntity<Object> createSchedule(@RequestBody ScheduleDto.ScheduleRegisterDto scheduleRegisterDto) {
        String token = scheduleRegisterDto.getToken();
        String id = scheduleRegisterDto.isIskakao() ? oAuthService.getUserInfo(token).get("email").toString() : jwtProvider.getPayload(token);
        System.out.println("id: " + id);
        System.out.println(scheduleRegisterDto.getId()==null);
        if (!(scheduleRegisterDto.getId()==null)) {
            String leaderEmail = id;
            System.out.println("leaderEmail: " + leaderEmail);
            id = scheduleRegisterDto.getId();
            GroupDto.GroupRegisterDto groupRegisterDto = new GroupDto.GroupRegisterDto();

            String tmpMembers = scheduleRegisterDto.getMembers() + "," + leaderEmail;
            String[] members = tmpMembers.split(",");
            groupRegisterDto.setGroupid(id);
            groupRegisterDto.setMembers(members);
            groupRegisterDto.setLeaderName(leaderEmail);
            groupController.createGroup(groupRegisterDto);
        }
        String scheduleName = scheduleRegisterDto.getScheduleName();
        Date appointment = scheduleRegisterDto.getAppointment();
        Date appointmentDue = scheduleRegisterDto.getAppointmentDue();
        String stringAddress = scheduleRegisterDto.getAddress();

        String newSchedule = String.valueOf(scheduleService.create(id, scheduleName, appointment, appointmentDue, stringAddress));
        String[] cvtArgs = {"convert.sh", "schedule", newSchedule};
        wideService.execPython(cvtArgs);
        String[] personalWeatherArgs = {"setWeather.sh", "False", newSchedule};
        String[] groupWeatherArgs = {"setWeather.sh", "True", newSchedule};

        if(scheduleService.isEmail(id)) wideService.execPython(personalWeatherArgs);
        else wideService.execPython(groupWeatherArgs);

        return ResponseEntity.ok().body("일정 생성 완료");
    }

    //일정 삭제
    @Transactional
    @DeleteMapping("/deleteSchedule")
    public ResponseEntity<Object> deleteSchedule(@RequestParam int scheduleNum) {
        scheduleService.deleteSchedule(scheduleNum);
        return ResponseEntity.ok().body("일정 삭제 완료");
    }

    //일정 수정
    @Transactional
    @PatchMapping("/modifySchedule")
    public ResponseEntity<Object> modifySchedule(@RequestBody ScheduleDto.ScheduleRegisterDto scheduleDto) {
        int num = scheduleDto.getNum();
        String scheduleName = scheduleDto.getScheduleName();
        Date appointment = scheduleDto.getAppointment();
        /*
        float lon = scheduleDto.getLon();
        float lat = scheduleDto.getLat();
        */
        String stringAddress = scheduleDto.getAddress();
        scheduleService.modifySchedule(num, scheduleName, appointment, stringAddress);
        return ResponseEntity.ok().body("일정 수정 완료");
    }

    @Transactional
    @PostMapping("/getUserGroupSchedule")
    public ResponseEntity<Object> getUserGroupSchedule(@RequestBody String email) {
        List<Schedule> userSchedule = scheduleService.getAllSchedules(email);
        List<Schedule> groupSchedules = new ArrayList<>();
        List<String> gruopids = groupService.findGroupsContainUser(email);
        for (String gruopid : gruopids) {
            List<Schedule> groupSchedule = scheduleService.getAllSchedules(gruopid);
            groupSchedules.addAll(groupSchedule);
        }
        userSchedule.addAll(groupSchedules);
        return ResponseEntity.ok().body(userSchedule);
    }

    @Transactional
    @PostMapping("/getAlert")
    public ResponseEntity<Object> getAlert(@RequestBody String email) {
        List<Alert> alerts =  wideService.getAllAlerts(email);
        List<String> alertPharses = new ArrayList<>();

        for(Alert a: alerts) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String date = format.format(a.getRecommend());
            String scheduleName = scheduleService.getSchedule(a.getNum()).getScheduleName();
            if(date.equals("2001-01-01")) {
                alertPharses.add(scheduleName + " 약속의 날씨가 \'" + a.getWeatherDesc() + "\'입니다.\n"
                        + "하지만, 가까운 시일 내에 추천할만한 일자가 없어요");
            }
            else {

                alertPharses.add(scheduleName + " 약속의 날씨가 \'" + a.getWeatherDesc() + "\'입니다.\n"
                        + date + "로 약속을 변경하는게 좋아보여요");
            }
        }

        return ResponseEntity.ok().body(alertPharses);
    }


}
