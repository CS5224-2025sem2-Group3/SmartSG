package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.UserProfile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProfileMapper {

    @Select("SELECT user_id, move_in_window, lease_preference, sleep_schedule, cleanliness, smoking, gender, max_budget FROM UserProfile WHERE user_id = #{userId}")
    @Results(id = "profileResult", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "moveInWindow", column = "move_in_window"),
            @Result(property = "leasePreference", column = "lease_preference"),
            @Result(property = "sleepSchedule", column = "sleep_schedule"),
            @Result(property = "cleanliness", column = "cleanliness"),
            @Result(property = "smoking", column = "smoking"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "maxBudget", column = "max_budget")
    })
    UserProfile findByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO UserProfile (user_id, move_in_window, lease_preference, sleep_schedule, cleanliness, smoking, gender, max_budget) " +
            "VALUES (#{userId}, #{moveInWindow}, #{leasePreference}, " +
            "CAST(#{sleepSchedule, typeHandler=org.apache.ibatis.type.EnumTypeHandler} AS sleep_schedule_enum), " +
            "CAST(#{cleanliness, typeHandler=org.apache.ibatis.type.EnumTypeHandler} AS cleanliness_enum), " +
            "#{smoking}, CAST(#{gender, typeHandler=org.apache.ibatis.type.EnumTypeHandler} AS gender_enum), #{maxBudget})")
    int insert(UserProfile profile);

    @Update("UPDATE UserProfile SET move_in_window = #{moveInWindow}, lease_preference = #{leasePreference}, " +
            "sleep_schedule = CAST(#{sleepSchedule, typeHandler=org.apache.ibatis.type.EnumTypeHandler} AS sleep_schedule_enum), " +
            "cleanliness = CAST(#{cleanliness, typeHandler=org.apache.ibatis.type.EnumTypeHandler} AS cleanliness_enum), " +
            "smoking = #{smoking}, gender = CAST(#{gender, typeHandler=org.apache.ibatis.type.EnumTypeHandler} AS gender_enum), " +
            "max_budget = #{maxBudget} WHERE user_id = #{userId}")
    int update(UserProfile profile);
}
