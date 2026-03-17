package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.Favorites;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    // 查询用户的所有收藏
    @Select("SELECT listing_id FROM Favorites WHERE user_id = #{userId}")
    List<Long> findListingIdsByUserId(@Param("userId") Long userId);

    // 检查某收藏是否存在
    @Select("SELECT COUNT(*) FROM Favorites WHERE user_id = #{userId} AND listing_id = #{listingId}")
    int exists(@Param("userId") Long userId, @Param("listingId") Long listingId);

    // 插入收藏
    @Insert("INSERT INTO Favorites (user_id, listing_id) VALUES (#{userId}, #{listingId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Favorites favorites);

    // 删除收藏
    @Delete("DELETE FROM Favorites WHERE user_id = #{userId} AND listing_id = #{listingId}")
    int delete(@Param("userId") Long userId, @Param("listingId") Long listingId);
}