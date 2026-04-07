package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.response.FavoriteResponse;
import com.nus.cs5224.smartsg.entity.Favorites;
import com.nus.cs5224.smartsg.mapper.FavoriteMapper;
import com.nus.cs5224.smartsg.service.serviceImpl.FavoriteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Nested
    @DisplayName("getFavorites")
    class GetFavoritesTests {

        @Test
        @DisplayName("F3: 获取收藏列表")
        void getFavorites_withResults() {
            when(favoriteMapper.findListingIdsByUserId(1L)).thenReturn(List.of(10L, 20L, 30L));

            List<FavoriteResponse> favorites = favoriteService.getFavorites(1L);

            assertEquals(3, favorites.size());
            assertEquals(10L, favorites.get(0).getListingId());
            assertEquals(20L, favorites.get(1).getListingId());
            assertEquals(30L, favorites.get(2).getListingId());
        }

        @Test
        @DisplayName("F6: 无收藏时返回空列表")
        void getFavorites_empty() {
            when(favoriteMapper.findListingIdsByUserId(999L)).thenReturn(Collections.emptyList());

            List<FavoriteResponse> favorites = favoriteService.getFavorites(999L);

            assertTrue(favorites.isEmpty());
        }
    }

    @Nested
    @DisplayName("addFavorite")
    class AddFavoriteTests {

        @Test
        @DisplayName("F1: 正常添加收藏")
        void addFavorite_success() {
            when(favoriteMapper.exists(1L, 10L)).thenReturn(0);

            assertDoesNotThrow(() -> favoriteService.addFavorite(1L, 10L));

            verify(favoriteMapper).insert(any(Favorites.class));
        }

        @Test
        @DisplayName("F2: 重复添加收藏 - 幂等操作不报错")
        void addFavorite_duplicate_idempotent() {
            when(favoriteMapper.exists(1L, 10L)).thenReturn(1);

            assertDoesNotThrow(() -> favoriteService.addFavorite(1L, 10L));

            verify(favoriteMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavoriteTests {

        @Test
        @DisplayName("F4: 正常删除收藏")
        void removeFavorite_success() {
            when(favoriteMapper.delete(1L, 10L)).thenReturn(1);

            assertDoesNotThrow(() -> favoriteService.removeFavorite(1L, 10L));

            verify(favoriteMapper).delete(1L, 10L);
        }

        @Test
        @DisplayName("F5: 删除不存在的收藏 - 幂等不报错")
        void removeFavorite_nonExistent_idempotent() {
            when(favoriteMapper.delete(1L, 999L)).thenReturn(0);

            assertDoesNotThrow(() -> favoriteService.removeFavorite(1L, 999L));
        }
    }
}
