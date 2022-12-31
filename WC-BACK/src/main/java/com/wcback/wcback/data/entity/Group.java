package com.wcback.wcback.data.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "groupid")
@IdClass(GroupPK.class)
public class Group {
    @Id
    @Column(nullable = false)
    private String groupid;

    @Id
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean leader = false;

}

