package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.request.ListingFilter;
import com.nus.cs5224.smartsg.dto.response.ListingDetailResponse;
import com.nus.cs5224.smartsg.dto.response.ListingSummaryResponse;
import com.nus.cs5224.smartsg.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @GetMapping
    public List<ListingSummaryResponse> getListings(
            @RequestParam(required = false) Double budgetMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableFrom,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) Double distance) {

        // 校验 university 和 distance 必须同时存在或同时不存在
        if ((university == null && distance != null) || (university != null && distance == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "university and distance must be provided together or both omitted");
        }

        // 如果提供了 university，确保其值为允许的枚举
        if (university != null && !List.of("NUS", "NTU", "SMU").contains(university)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "university must be one of: NUS, NTU, SMU");
        }

        ListingFilter filter = new ListingFilter();
        filter.setBudgetMax(budgetMax);
        filter.setAvailableFrom(availableFrom);
        filter.setUniversity(university);
        filter.setDistance(distance);

        return listingService.getListings(filter);
    }

    @GetMapping("/{id}")
    public ListingDetailResponse getListingDetail(@PathVariable Long id) {
        return listingService.getListingDetail(id);
    }
}