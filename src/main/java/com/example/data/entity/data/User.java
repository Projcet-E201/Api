package com.example.data.entity.data;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class User {

    /* ID 또는 사원번호 */
    @Id @GeneratedValue
    @Column(name="user_id")
    private Long id;

    @Column
    private String password;

    @Column
    private String name;

    @Column(unique = true)
    private String email;

    /* 권한 */
    @Column
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Alarm> alarms = new ArrayList<>();

}
