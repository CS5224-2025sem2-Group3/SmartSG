package com.nus.cs5224.smartsg.entity;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private String name;
    private String email;
    private String passwordHash;
    private String school;
}