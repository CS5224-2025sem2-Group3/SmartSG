package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.Group;
import com.nus.cs5224.smartsg.entity.GroupMember;

import org.apache.ibatis.annotations.*;


@Mapper
public interface GroupMapper {
    // --- Group 表的操作 ---
    @Select("SELECT * FROM \"Group\" WHERE group_id = #{groupId}")
    @Results(id = "groupResult", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "listingId", column = "listing_id"),
            @Result(property = "maxPeople", column = "max_people"),
            @Result(property = "curPeople", column = "cur_people")
    })
    Group findById(@Param("groupId") int groupId);

    @Insert("INSERT INTO \"Group\" (listing_id, status, max_people, cur_people) VALUES (#{listingId}, #{status}, #{maxPeople}, #{curPeople})")
    @Options(useGeneratedKeys = true, keyProperty = "groupId", keyColumn = "group_id")
    int insertGroup(Group group);

    @Update("UPDATE \"Group\" SET cur_people = #{curPeople} WHERE group_id = #{groupId}")
    int updateCurPeople(@Param("groupId") int groupId, @Param("curPeople") int curPeople);

    @Update("UPDATE \"Group\" SET status = #{status} WHERE group_id = #{groupId}")
    int updateStatus(@Param("groupId") int groupId, @Param("status") String status);

    @Delete("DELETE FROM \"Group\" WHERE group_id = #{groupId}")
    int deleteGroup(@Param("groupId") int groupId);

    // --- GroupMember 表的操作 ---
    @Insert("INSERT INTO group_member (group_id, user_id, role) VALUES (#{groupId}, #{userId}, #{role})")
    int insertMember(GroupMember member);

    @Delete("DELETE FROM group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteMember(@Param("groupId") int groupId, @Param("userId") long userId);

    @Delete("DELETE FROM group_member WHERE group_id = #{groupId}")
    int deleteAllMembersByGroupId(@Param("groupId") int groupId);

    @Select("SELECT * FROM group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    @Results(id = "memberResult", value = {
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "userId", column = "user_id")
    })
    GroupMember findMember(@Param("groupId") int groupId, @Param("userId") long userId);
}
