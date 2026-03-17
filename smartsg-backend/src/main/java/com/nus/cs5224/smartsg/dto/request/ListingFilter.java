package com.nus.cs5224.smartsg.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ListingFilter {
    private Double budgetMax;           // 每位租客的最高预算（rent_per_tenant）
    private LocalDate availableFrom;     // 最早可入住日期
    private String university;           // 大学缩写：NUS, NTU, SMU（与 distance 绑定出现）
    private Double distance;             // 距该大学的最大距离（公里），与 university 一起提供
}