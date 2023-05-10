package com.example.data.entity.global;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Client {
    @Id
    @GeneratedValue
    @Column(name = "client_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "client")
    private List<DataType> settings = new ArrayList<>();
}
