package com.nus.cs5224.smartsg.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Listing {
    private Long listingId;
    private String title;
    private String address;
    private Integer rent;
    private Integer lease;
    private String flatType;
    private LocalDate availableFrom;
    private Boolean fullyFurnished;
    private Boolean cookingAllowed;
    private String uniDistances;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Integer totalTenants;
    private BigDecimal rentPerTenant;
}