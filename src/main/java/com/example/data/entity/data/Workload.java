package com.example.data.entity.data;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class Workload {

    @Id
    private long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    private SensorInfo sensorInfo;

    @NotNull
    private Double value = 0.0;

}