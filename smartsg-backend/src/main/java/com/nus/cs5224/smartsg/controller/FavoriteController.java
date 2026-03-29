package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.response.FavoriteResponse;
import com.nus.cs5224.smartsg.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * GET /api/favorites
     * Get all favorites for current user
     */
    @GetMapping
    public List<FavoriteResponse> getFavorites(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("currentUserId");
        return favoriteService.getFavorites(userId);
    }

    /**
     * POST /api/favorites/{listingId}
     * Add a listing to favorites
     */
    @PostMapping("/{listingId}")
    public Map<String, Boolean> addFavorite(@PathVariable Long listingId,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("currentUserId");
        favoriteService.addFavorite(userId, listingId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    /**
     * DELETE /api/favorites/{listingId}
     * Remove a listing from favorites
     */
    @DeleteMapping("/{listingId}")
    public Map<String, Boolean> removeFavorite(@PathVariable Long listingId,
                                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("currentUserId");
        favoriteService.removeFavorite(userId, listingId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}