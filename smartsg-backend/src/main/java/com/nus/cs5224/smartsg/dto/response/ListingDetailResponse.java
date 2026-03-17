package com.nus.cs5224.smartsg.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingDetailResponse {
    private Long id;
    private String title;
    private String type;
    private Integer totalRent;
    private LocalDate availableFrom;
    private Integer totalTenants;
    private BigDecimal rentPerTenant;
    private Map<String, Double> uniDistances;   // 解析后的距离
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private List<String> facilities;             // 根据布尔字段生成
}