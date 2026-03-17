package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.response.ListingDetailResponse;
import com.nus.cs5224.smartsg.dto.response.ListingSummaryResponse;
import com.nus.cs5224.smartsg.dto.response.UniversityResponse;
import com.nus.cs5224.smartsg.dto.request.ListingFilter;

import java.util.List;

public interface ListingService {
    public List<UniversityResponse> getUniversities();
    public List<ListingSummaryResponse> getListings(ListingFilter filter);
    public ListingDetailResponse getListingDetail(Long id);
}
