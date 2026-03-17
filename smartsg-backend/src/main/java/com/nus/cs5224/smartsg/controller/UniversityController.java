package com.nus.cs5224.smartsg.controller;


import com.nus.cs5224.smartsg.dto.response.UniversityResponse;
import com.nus.cs5224.smartsg.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/universities")
public class UniversityController {

    @Autowired
    private ListingService listingService;

    @GetMapping
    public List<UniversityResponse> getUniversities() {
        return listingService.getUniversities();
    }
}