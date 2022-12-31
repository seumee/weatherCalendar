package com.wcback.wcback.service;

import com.wcback.wcback.data.entity.Alert;
import com.wcback.wcback.data.entity.Group;
import com.wcback.wcback.data.repository.AlertRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class WideService {
    GroupService groupService;
    ScheduleService scheduleService;
    AlertRepository alertRepository;

    public void execPython(String[] args) {
        /*------------call Python----------*/
        ProcessBuilder processBuilder = new ProcessBuilder();
        String pyDir = "/home/capstone/capstone_python/";
        //processBuilder.command(pyDir + shName);
        int argc = args.length;
        if(argc == 2) {
            processBuilder.command(pyDir + args[0], args[1]);
        } else if(argc == 3) {
            processBuilder.command(pyDir + args[0], args[1], args[2]);
        }

        try {
            // Run script
            Process process = processBuilder.start();
            // Read output
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            System.out.println(output.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<Alert> getAllAlerts(String ID) {
        List<String> groups = groupService.findGroupsContainUser(ID);
        List<Alert> alerts = new ArrayList<>();
        List<Integer> nums = scheduleService.getNumOfSchedules(scheduleService.getAllSchedules(ID));
        for(String g: groups) {
            nums.addAll(scheduleService.getNumOfSchedules(scheduleService.getAllSchedules(g)));
        }

        for(Integer num : nums) {
            alerts.addAll(alertRepository.findByNum(num));
        }
        return alerts;
    }
}
