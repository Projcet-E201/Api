package com.example.data.entity.global;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String authority;

    // 기기 설정 값
    @OneToMany(mappedBy = "member")
    private List<MachineSetting> settings = new ArrayList<>();
}
