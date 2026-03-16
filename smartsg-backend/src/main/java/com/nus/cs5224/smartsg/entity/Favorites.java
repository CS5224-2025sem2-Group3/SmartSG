package com.nus.cs5224.smartsg.entity;

import lombok.Data;

@Data
public class Favorites {
    private Long id;
    private Long userId;
    private Long listingId;
}