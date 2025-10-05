package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int level;
    private int cash;
    private int rubi;
    private int ticketCount;
    private int stage;
}
