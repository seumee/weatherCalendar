package com.wcback.wcback.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Schedule")
public class Schedule {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int num;

    @Column(nullable = false)
    private String scheduleName;

    private String email;

    private String groupID;

    @Column(nullable = false)
    private Date appointment;

    @Column(nullable = false)
    private Date appointmentDue;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true)
    private float lon;

    @Column(nullable = true)
    private float lat;

    private String weather;

    @Column(nullable = true)
    private String weatherDesc;

    @Column(nullable = true)
    private String weatherIcon;

}