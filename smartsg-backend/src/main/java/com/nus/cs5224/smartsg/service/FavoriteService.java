package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.response.FavoriteResponse;

import java.util.List;

public interface FavoriteService {
    public List<FavoriteResponse> getFavorites(Long userId);
    public void addFavorite(Long userId, Long listingId);
    public void removeFavorite(Long userId, Long listingId);
}