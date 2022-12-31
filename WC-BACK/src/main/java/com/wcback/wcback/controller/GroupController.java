package com.wcback.wcback.controller;

import com.wcback.wcback.config.JwtProvider;
import com.wcback.wcback.data.dto.Group.GroupDto;
import com.wcback.wcback.exception.user.AlreadyExistException;
import com.wcback.wcback.service.GroupService;
import com.wcback.wcback.service.OAuthService;
import com.wcback.wcback.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/group")
@AllArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final OAuthService oAuthService;
    private final JwtProvider jwtProvider;

    // 그룹 조회
    @Transactional
    @GetMapping
    public ResponseEntity<Object> getGroup(@RequestParam String groupid){
        try {
            if (!groupService.checkGroup(groupid)) {
                throw new AlreadyExistException("존재하지 않는 그룹입니다.");
            }
            return ResponseEntity.ok().body(groupService.findById(groupid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 그룹 생성
    @Transactional
    @PostMapping
    public ResponseEntity<Object> createGroup(@RequestBody GroupDto.GroupRegisterDto groupRegisterDto) {
        String groupid = groupRegisterDto.getGroupid();
        String[] members = groupRegisterDto.getMembers();
        String leaderName = groupRegisterDto.getLeaderName();
        try {
            if (groupService.checkGroup(groupid)) {
                throw new AlreadyExistException("이미 존재하는 그룹명입니다.");
            }
            return ResponseEntity.ok().body(groupService.loopMembers(groupid, members, leaderName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 그룹 삭제
    @Transactional
    @DeleteMapping
    public ResponseEntity<Object> deleteGroup(@RequestParam String groupName) {
        groupService.deleteById(groupName);
        return ResponseEntity.ok().body("그룹 삭제 완료");
    }



    // 그룹 탈퇴
    @Transactional
    @DeleteMapping("/groupOut")
    public ResponseEntity<Object> GroupOut(@RequestParam String groupName, String email) {
        groupService.groupOut(groupName,email);
        return ResponseEntity.ok().body("탈퇴 완료");
    }

    // 유저가 속한 그룹들 모두 찾기
    @Transactional
    @PostMapping("/getGroupsContainUser")
    public ResponseEntity<Object> getGroupsContainUser(@RequestBody GroupDto.GroupFindiDto groupFindDto) {
        String email = (groupFindDto.isIskakao())
                ? oAuthService.getUserInfo(groupFindDto.getToken()).get("email").toString()
                : jwtProvider.getPayload(groupFindDto.getToken());
        return ResponseEntity.ok().body(groupService.findGroupsContainUser(email));
    }

    // 유저가 속한 그룹별로 Schedule반환하기
    @Transactional
    @PostMapping("/getAllGroupSchedule")
    public ResponseEntity<Object> getAllGroupSchedule(@RequestBody GroupDto.GroupFindiDto groupFindDto) {
        System.out.println((boolean) groupFindDto.isIskakao());
        System.out.println(groupFindDto.getToken());
        String email = getEmailByAnyToken(groupFindDto.getToken(), groupFindDto.isIskakao());
        return ResponseEntity.ok().body(groupService.findGroupSchedule(email));
    }

    public String getEmailByAnyToken(String token, boolean isKakao) {
        return (isKakao)
                ? oAuthService.getUserInfo(token).get("email").toString()
                : jwtProvider.getPayload(token);
    }
}

