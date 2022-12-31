package com.wcback.wcback.data.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupPK implements Serializable {
    private String groupid;
    private String email;
}
