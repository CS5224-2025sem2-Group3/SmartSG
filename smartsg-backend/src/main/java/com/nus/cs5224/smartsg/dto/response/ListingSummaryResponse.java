package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingSummaryResponse {
    private Long id;
    private String title;
    private String type;          // flat_type
    private Integer totalRent;     // rent
    private LocalDate availableFrom;
}