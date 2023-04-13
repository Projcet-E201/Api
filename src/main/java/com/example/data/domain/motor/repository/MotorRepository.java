package com.example.data.domain.motor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.data.entity.data.Motor;

public interface MotorRepository extends JpaRepository<Motor, Long> {
}
