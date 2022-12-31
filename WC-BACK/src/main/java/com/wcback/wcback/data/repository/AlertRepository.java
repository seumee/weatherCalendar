package com.wcback.wcback.data.repository;

import com.wcback.wcback.data.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AlertRepository extends JpaRepository<Alert, Object> {
    List<Alert> findByNum(int num);
}
