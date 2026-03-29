package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private Long userId;
    private String name;
    private Integer budgetMax;
    private String moveInWindow;
    private int matchScore;
}
