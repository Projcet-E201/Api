package com.example.data.entity.global;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "settings")
public class MachineSetting {
    @Id
    @GeneratedValue
    @Column(name = "machine_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime changeTime; // 교체 일자

    // 그래프 설정

}
