package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponse {
    private String value;  // 缩写，如 "NUS"
    private String label;  // 全名，如 "National University of Singapore"
}