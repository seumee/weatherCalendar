package com.wcback.wcback.controller;

import com.wcback.wcback.config.JwtProvider;
import com.wcback.wcback.data.dto.User.UserDto;
import com.wcback.wcback.data.entity.User;
import com.wcback.wcback.exception.user.AlreadyExistException;
import com.wcback.wcback.service.OAuthService;
import com.wcback.wcback.service.UserService;
import com.wcback.wcback.service.WideService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class OAuthConroller {
    private final OAuthService oAuth;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final WideService wideService;

    // 카카오 로그인 + 회원가입
    @ResponseBody
    @GetMapping("/kakao/callback")
    public ResponseEntity<Object> kakaoCallback(@RequestParam String code, HttpServletResponse response) {
        String token = oAuth.getKakaoAccessToken(code);
        HashMap<String ,Object> userInfo = oAuth.getUserInfo(token);
        String email = userInfo.get("email").toString();
        boolean status = false;
        // 아직 가입되지 않은 이메일은 먼저 회원가입 처리
        if(!userService.checkUser(email)) {
            UserDto.UserRegisterDto userRequestDto = new UserDto.UserRegisterDto();
            userRequestDto.setName(userInfo.get("userName").toString());
            userRequestDto.setEmail(userInfo.get("email").toString());
            userRequestDto.setProfile_image(userInfo.get("profile_image").toString());
            userRequestDto.setIskakao(true);

            try {
                userService.register(userRequestDto);
                status = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }



        // 로그인 처리
        User loginUser = userService.findUserByEmail(email);
        String JwtToken = jwtProvider.createToken(email);
        userService.updateToken(loginUser.getEmail(), JwtToken);

        UserDto.UserLoginDto userInfoDto = new UserDto.UserLoginDto();
        userInfoDto.setName(loginUser.getName());
        userInfoDto.setToken(token);
        System.out.println(userInfoDto);
        HttpHeaders headers = new HttpHeaders();
        if (status) {
            headers.setLocation(URI.create("http://seumchae.iptime.org:8994/kakaoaddress.html"));
        }
        else {
            headers.setLocation(URI.create("http://seumchae.iptime.org:8994/main.html"));
        }
        headers.set("kakao-token",token);
        Cookie cookie = new Cookie("kakao-token", token);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    };


}