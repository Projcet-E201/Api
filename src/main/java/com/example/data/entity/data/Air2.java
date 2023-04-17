package com.example.data.entity.data;

import javax.persistence.*;

import com.example.data.entity.global.BaseEntity;
import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString
@Builder
@Entity
public class Air2 extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 센서 상세정보 */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    private SensorInfo sensorInfo;

    @NotNull
    private Double value;
}
