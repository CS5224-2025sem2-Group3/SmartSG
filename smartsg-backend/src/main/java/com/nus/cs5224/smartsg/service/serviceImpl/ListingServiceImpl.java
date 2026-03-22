package com.nus.cs5224.smartsg.service.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nus.cs5224.smartsg.dto.request.ListingFilter;
import com.nus.cs5224.smartsg.dto.response.ListingDetailResponse;
import com.nus.cs5224.smartsg.dto.response.ListingSummaryResponse;
import com.nus.cs5224.smartsg.dto.response.UniversityResponse;
import com.nus.cs5224.smartsg.entity.Listing;
import com.nus.cs5224.smartsg.mapper.ListingMapper;
import com.nus.cs5224.smartsg.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListingServiceImpl implements ListingService {

    @Autowired
    private ListingMapper listingMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<UniversityResponse> getUniversities() {
        return Arrays.asList(
                new UniversityResponse("NUS", "National University of Singapore"),
                new UniversityResponse("NTU", "Nanyang Technological University"),
                new UniversityResponse("SMU", "Singapore Management University")
        );
    }

    @Override
    public List<ListingSummaryResponse> getListings(ListingFilter filter) {
        List<Listing> listings = listingMapper.findListings(filter);
        return listings.stream().map((listing) -> convertToSummary(listing, filter)).collect(Collectors.toList());
    }

    @Override
    public ListingDetailResponse getListingDetail(Long id) {
        Listing listing = listingMapper.findById(id);
        if (listing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        }
        return convertToDetail(listing);
    }

    private ListingSummaryResponse convertToSummary(Listing listing, ListingFilter filter) {
        Map<String, Double> distances = parseDistances(listing.getUniDistances());

        return new ListingSummaryResponse(
                listing.getListingId(),
                listing.getTitle(),
                listing.getFlatType(),
                listing.getRent(),
                listing.getAvailableFrom(),
                listing.getTotalTenants(),
                listing.getRentPerTenant(),
                filter.getUniversity() != null ? distances.get(filter.getUniversity()) : null,
                listing.getImageUrl(),
                buildFacilities(listing),
                listing.getLease() != null ? List.of(listing.getLease()) : List.of()
        );
    }

    private ListingDetailResponse convertToDetail(Listing listing) {
        Map<String, Double> distances = parseDistances(listing.getUniDistances());

        return new ListingDetailResponse(
                listing.getListingId(),
                listing.getTitle(),
                listing.getFlatType(),
                listing.getRent(),
                listing.getAvailableFrom(),
                listing.getTotalTenants(),
                listing.getRentPerTenant(),
                distances,
                listing.getLatitude(),
                listing.getLongitude(),
                listing.getImageUrl(),
                buildFacilities(listing)
        );
    }

    private Map<String, Double> parseDistances(String rawDistances) {
        if (rawDistances != null) {
            try {
                return objectMapper.readValue(rawDistances, new TypeReference<Map<String, Double>>() {});
            } catch (Exception e) {
                return Map.of();
            }
        }

        return Map.of();
    }

    private List<String> buildFacilities(Listing listing) {
        List<String> facilities = new ArrayList<>();
        if (Boolean.TRUE.equals(listing.getFullyFurnished())) {
            facilities.add("Fully Furnished");
        }
        if (Boolean.TRUE.equals(listing.getCookingAllowed())) {
            facilities.add("Cooking Allowed");
        }
        return facilities;
    }
}
