package com.example.data.entity.global;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "type")
public class DataType {
    @Id
    @GeneratedValue
    @Column(name = "type_id")
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private int warning_value;

    private int impact_value;
}
