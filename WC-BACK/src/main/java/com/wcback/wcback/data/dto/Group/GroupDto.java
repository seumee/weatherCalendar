
package com.wcback.wcback.data.dto.Group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GroupDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupRegisterDto {
        private String groupid;
        private String[] members;
        private String leaderName;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupFindiDto {
        private String token;
        private boolean iskakao;
    }

}

