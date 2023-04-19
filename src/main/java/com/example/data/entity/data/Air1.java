package com.example.data.entity.data;

import javax.persistence.*;

import com.example.data.entity.global.BaseEntity;
import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class Air1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*  시간 데이터  */
//    @Column(name = "time")
//    @org.springframework.data.annotation.Id
//    private Instant time;

    /* 센서 상세정보 */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    private SensorInfo sensorInfo;

    @NotNull
    private Double value = 0.0;
}
