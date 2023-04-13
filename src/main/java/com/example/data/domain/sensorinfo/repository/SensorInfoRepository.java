package com.example.data.domain.sensorinfo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.data.entity.data.SensorInfo;

public interface SensorInfoRepository extends JpaRepository<SensorInfo, Long> {

	Optional<SensorInfo> findBySensorId(String sensorId);
}
