package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏列表项，仅包含房源ID
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long listingId;
}