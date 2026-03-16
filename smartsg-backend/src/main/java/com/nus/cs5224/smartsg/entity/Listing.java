package com.nus.cs5224.smartsg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listing {
    private Long listingId;
    private String title;
    private String address;
    private BigDecimal rent;
    private Integer lease;
    private String flatType;
    private LocalDate availableFrom;
}