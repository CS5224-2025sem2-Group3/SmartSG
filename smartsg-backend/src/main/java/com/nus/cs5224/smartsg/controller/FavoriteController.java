package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.response.FavoriteResponse;
import com.nus.cs5224.smartsg.dto.response.UserResponse;
import com.nus.cs5224.smartsg.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 从 session 中获取当前登录用户的 ID，若未登录则抛出 401 异常
     */
    private Long getCurrentUserId(HttpSession session) {
        UserResponse currentUser = (UserResponse) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
        return currentUser.getId();
    }

    /**
     * GET /api/favorites
     * 返回当前用户的收藏房源ID列表
     */
    @GetMapping
    public List<FavoriteResponse> getFavorites(HttpSession session) {
        Long userId = getCurrentUserId(session);
        return favoriteService.getFavorites(userId);
    }

    /**
     * POST /api/favorites/{listingId}
     * 添加房源到收藏
     */
    @PostMapping("/{listingId}")
    public Map<String, Boolean> addFavorite(@PathVariable Long listingId, HttpSession session) {
        Long userId = getCurrentUserId(session);
        favoriteService.addFavorite(userId, listingId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    /**
     * DELETE /api/favorites/{listingId}
     * 从收藏中移除房源
     */
    @DeleteMapping("/{listingId}")
    public Map<String, Boolean> removeFavorite(@PathVariable Long listingId, HttpSession session) {
        Long userId = getCurrentUserId(session);
        favoriteService.removeFavorite(userId, listingId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}