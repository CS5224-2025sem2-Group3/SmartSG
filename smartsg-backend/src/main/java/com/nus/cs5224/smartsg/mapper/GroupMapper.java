package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.Group;
import com.nus.cs5224.smartsg.entity.GroupMember;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMapper {
    // --- Group table ---
    @Select("SELECT * FROM \"Group\" WHERE group_id = #{groupId}")
    @Results(id = "groupResult", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "listingId", column = "listing_id"),
            @Result(property = "maxPeople", column = "max_people"),
            @Result(property = "curPeople", column = "cur_people")
    })
    Group findById(@Param("groupId") int groupId);

    @Select("SELECT g.* FROM \"Group\" g " +
            "JOIN GroupMember gm ON g.group_id = gm.group_id " +
            "WHERE gm.user_id = #{userId}")
    @Results(id = "groupResult2", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "listingId", column = "listing_id"),
            @Result(property = "maxPeople", column = "max_people"),
            @Result(property = "curPeople", column = "cur_people")
    })
    List<Group> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM \"Group\" WHERE listing_id = #{listingId}")
    @Results(id = "groupResult3", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "listingId", column = "listing_id"),
            @Result(property = "maxPeople", column = "max_people"),
            @Result(property = "curPeople", column = "cur_people")
    })
    List<Group> findByListingId(@Param("listingId") Long listingId);

    @Insert("INSERT INTO \"Group\" (listing_id, status, max_people, cur_people) VALUES (#{listingId}, CAST(#{status} AS group_status_enum), #{maxPeople}, #{curPeople})")
    @Options(useGeneratedKeys = true, keyProperty = "groupId", keyColumn = "group_id")
    int insertGroup(Group group);

    @Update("UPDATE \"Group\" SET cur_people = #{curPeople} WHERE group_id = #{groupId}")
    int updateCurPeople(@Param("groupId") int groupId, @Param("curPeople") int curPeople);

    @Update("UPDATE \"Group\" SET status = CAST(#{status} AS group_status_enum) WHERE group_id = #{groupId}")
    int updateStatus(@Param("groupId") int groupId, @Param("status") String status);

    @Delete("DELETE FROM \"Group\" WHERE group_id = #{groupId}")
    int deleteGroup(@Param("groupId") int groupId);

    // --- GroupMember table ---
    @Insert("INSERT INTO GroupMember (group_id, user_id, role) VALUES (#{groupId}, #{userId}, CAST(#{role} AS group_role_enum))")
    int insertMember(GroupMember member);

    @Delete("DELETE FROM GroupMember WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteMember(@Param("groupId") int groupId, @Param("userId") long userId);

    @Delete("DELETE FROM GroupMember WHERE group_id = #{groupId}")
    int deleteAllMembersByGroupId(@Param("groupId") int groupId);

    @Select("SELECT * FROM GroupMember WHERE group_id = #{groupId} AND user_id = #{userId}")
    @Results(id = "memberResult", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "userId", column = "user_id")
    })
    GroupMember findMember(@Param("groupId") int groupId, @Param("userId") long userId);

    @Select("SELECT gm.group_id, gm.user_id, gm.role, u.name, up.max_budget, up.move_in_window, up.lease_preference " +
            "FROM GroupMember gm " +
            "JOIN \"User\" u ON gm.user_id = u.user_id " +
            "LEFT JOIN UserProfile up ON gm.user_id = up.user_id " +
            "WHERE gm.group_id = #{groupId}")
    @Results(id = "memberDetailResult", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "role", column = "role"),
            @Result(property = "name", column = "name"),
            @Result(property = "budgetMax", column = "max_budget"),
            @Result(property = "moveInWindow", column = "move_in_window"),
            @Result(property = "leasePreference", column = "lease_preference")
    })
    List<GroupMember> findMembersByGroupId(@Param("groupId") int groupId);
}
