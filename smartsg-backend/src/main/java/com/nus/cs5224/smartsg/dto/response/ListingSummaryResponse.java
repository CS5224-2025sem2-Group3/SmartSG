package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingSummaryResponse {
    private Long id;
    private String title;
    private String type;          // flat_type
    private Integer totalRent;     // rent
    private LocalDate availableFrom;
    private Integer totalTenants;
    private BigDecimal rentPerTenant;
    private Double distance;
    private String imageUrl;
    private List<String> facilities;
    private List<Integer> leaseOptions;
}
