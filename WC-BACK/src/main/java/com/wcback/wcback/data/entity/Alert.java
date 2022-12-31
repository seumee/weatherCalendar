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
@Table(name = "alert")
public class Alert {
    @Id
    @Column(unique = true, nullable = false)
    private int num;

    private String weatherDesc;

    private Date recommend;
}