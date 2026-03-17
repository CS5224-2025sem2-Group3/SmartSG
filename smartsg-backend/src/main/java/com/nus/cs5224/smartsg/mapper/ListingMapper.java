package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.dto.request.ListingFilter;
import com.nus.cs5224.smartsg.entity.Listing;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ListingMapper {

    @Select("<script>" +
            "SELECT listing_id, title, address, rent, lease, flat_type, available_from, " +
            "fully_furnished, cooking_allowed, uni_distances, latitude, longitude, image_url, " +
            "total_tenants, rent_per_tenant " +
            "FROM Listing " +
            "WHERE 1=1 " +
            "<if test='filter.budgetMax != null'> AND rent_per_tenant &lt;= #{filter.budgetMax} </if>" +
            "<if test='filter.availableFrom != null'> AND available_from &gt;= #{filter.availableFrom} </if>" +
            "<if test='filter.university != null and filter.distance != null'> " +
            "   <choose>" +
            "       <when test='filter.university == \"NUS\"'> AND (uni_distances-&gt;&gt;'NUS')::float &lt;= #{filter.distance} </when>" +
            "       <when test='filter.university == \"NTU\"'> AND (uni_distances-&gt;&gt;'NTU')::float &lt;= #{filter.distance} </when>" +
            "       <when test='filter.university == \"SMU\"'> AND (uni_distances-&gt;&gt;'SMU')::float &lt;= #{filter.distance} </when>" +
            "   </choose>" +
            "</if>" +
            "</script>")
    @Results(id = "listingResult", value = {
            @Result(property = "listingId", column = "listing_id"),
            @Result(property = "flatType", column = "flat_type"),
            @Result(property = "availableFrom", column = "available_from"),
            @Result(property = "fullyFurnished", column = "fully_furnished"),
            @Result(property = "cookingAllowed", column = "cooking_allowed"),
            @Result(property = "uniDistances", column = "uni_distances"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "totalTenants", column = "total_tenants"),
            @Result(property = "rentPerTenant", column = "rent_per_tenant")
    })
    List<Listing> findListings(@Param("filter") ListingFilter filter);

    @Select("SELECT listing_id, title, address, rent, lease, flat_type, available_from, " +
            "fully_furnished, cooking_allowed, uni_distances, latitude, longitude, image_url, " +
            "total_tenants, rent_per_tenant " +
            "FROM Listing WHERE listing_id = #{id}")
    @Results(id = "listingDetailResult", value = {
            @Result(property = "listingId", column = "listing_id"),
            @Result(property = "flatType", column = "flat_type"),
            @Result(property = "availableFrom", column = "available_from"),
            @Result(property = "fullyFurnished", column = "fully_furnished"),
            @Result(property = "cookingAllowed", column = "cooking_allowed"),
            @Result(property = "uniDistances", column = "uni_distances"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "totalTenants", column = "total_tenants"),
            @Result(property = "rentPerTenant", column = "rent_per_tenant")
    })
    Listing findById(@Param("id") Long id);
}