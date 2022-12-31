package com.wcback.wcback.service;

import com.wcback.wcback.data.dto.User.UserDto;
import com.wcback.wcback.data.entity.User;
import com.wcback.wcback.data.repository.UserRepository;
import com.wcback.wcback.exception.user.AlreadyExistException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // 이메일 중복 체크
    public boolean checkUser(String email) {
        return userRepository.existsByEmail(email);
    }

    // 이메일로 유저 찾기
    public User findUserByEmail(String email) throws NoSuchElementException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일입니다."));
    }

    // 회원가입
    public User register(UserDto.UserRegisterDto userRequest) {
        User user = new User();
        BeanUtils.copyProperties(userRequest, user);
        User resultValue = userRepository.save(user);
        userRepository.flush();
        return resultValue;
    }

    // DB에 존재하는 토큰 바꾸기
    public void updateToken (String email, String token) {
        User user = findUserByEmail(email);
        user.setToken(token);
        userRepository.save(user);
    }

    // 회원정보 수정
    public User modify(UserDto.UserRegisterDto userRequest) {
        User originUser = userRepository.findByEmail(userRequest.getEmail()).get();
        BeanUtils.copyProperties(userRequest, originUser);
        return userRepository.save(originUser);
    }

    // 회원 탈퇴
    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }

    // 카카오 주소 정보 얻어오기
    public void getKakaoAddress(String email, String address) {
        User user = userRepository.findByEmail(email).get();
        user.setAddress(address);
        userRepository.save(user);
    }

}
