package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.UserProfile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MatchMapper {

    /**
     * Get all UserProfiles of users who are NOT already members of the given group.
     * Also excludes the requesting user (currentUserId).
     */
    @Select("SELECT up.user_id, up.move_in_window, up.lease_preference, " +
            "up.sleep_schedule, up.cleanliness, up.smoking, up.gender, up.max_budget, " +
            "u.name " +
            "FROM UserProfile up " +
            "JOIN \"User\" u ON up.user_id = u.user_id " +
            "WHERE up.user_id != #{currentUserId} " +
            "AND up.user_id NOT IN (" +
            "  SELECT user_id FROM GroupMember WHERE group_id = #{groupId}" +
            ")")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "moveInWindow", column = "move_in_window"),
            @Result(property = "leasePreference", column = "lease_preference"),
            @Result(property = "sleepSchedule", column = "sleep_schedule"),
            @Result(property = "maxBudget", column = "max_budget")
    })
    List<UserProfile> findCandidates(@Param("groupId") int groupId,
                                     @Param("currentUserId") Long currentUserId);
}
