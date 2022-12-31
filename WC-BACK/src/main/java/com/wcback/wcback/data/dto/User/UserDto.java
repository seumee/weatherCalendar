package com.wcback.wcback.data.dto.User;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRegisterDto {
        private String email;
        private String name;
        private String pwd;
        private String profile_image;
        private String address;
        private float lat;
        private float lon;
        private boolean iskakao = false;
        private String token;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLoginDto {
        private String email;
        private String name;
        private String pwd;
        private String profile_image;
        private float lat;
        private float lon;
        private String token;
    }

}
