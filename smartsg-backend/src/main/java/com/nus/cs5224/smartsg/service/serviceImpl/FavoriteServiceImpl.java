package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.response.FavoriteResponse;
import com.nus.cs5224.smartsg.entity.Favorites;
import com.nus.cs5224.smartsg.mapper.FavoriteMapper;
import com.nus.cs5224.smartsg.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    // 获取用户的收藏列表
    public List<FavoriteResponse> getFavorites(Long userId) {
        List<Long> listingIds = favoriteMapper.findListingIdsByUserId(userId);
        return listingIds.stream()
                .map(listingId -> new FavoriteResponse(listingId))
                .collect(Collectors.toList());
    }

    // 添加收藏
    @Transactional
    public void addFavorite(Long userId, Long listingId) {
        // 检查是否已存在
        if (favoriteMapper.exists(userId, listingId) > 0) {
            // 直接返回，不抛异常
            return;
        }
        Favorites fav = new Favorites();
        fav.setUserId(userId);
        fav.setListingId(listingId);
        favoriteMapper.insert(fav);
    }

    // 删除收藏
    @Transactional
    public void removeFavorite(Long userId, Long listingId) {
        int deleted = favoriteMapper.delete(userId, listingId);
        // 即使删除0条，也返回成功，无需处理
    }
}