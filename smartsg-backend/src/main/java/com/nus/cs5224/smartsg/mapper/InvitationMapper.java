package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.GroupRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InvitationMapper {

    @Select("SELECT gr.request_id, gr.group_id, gr.sender_id, gr.receiver_id, gr.status, " +
            "g.listing_id, l.title AS listing_title, u.name AS sender_name " +
            "FROM GroupRequest gr " +
            "JOIN \"Group\" g ON gr.group_id = g.group_id " +
            "JOIN Listing l ON g.listing_id = l.listing_id " +
            "JOIN \"User\" u ON gr.sender_id = u.user_id " +
            "WHERE gr.receiver_id = #{userId}")
    @Results({
            @Result(property = "requestId", column = "request_id"),
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "senderId", column = "sender_id"),
            @Result(property = "receiverId", column = "receiver_id")
    })
    List<GroupRequest> findByReceiverId(@Param("userId") Long userId);

    @Select("SELECT * FROM GroupRequest WHERE request_id = #{requestId}")
    @Results({
            @Result(property = "requestId", column = "request_id"),
            @Result(property = "groupId", column = "group_id"),
            @Result(property = "senderId", column = "sender_id"),
            @Result(property = "receiverId", column = "receiver_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    GroupRequest findById(@Param("requestId") int requestId);

    @Insert("INSERT INTO GroupRequest (group_id, sender_id, receiver_id, status) " +
            "VALUES (#{groupId}, #{senderId}, #{receiverId}, CAST('pending' AS request_status_enum))")
    @Options(useGeneratedKeys = true, keyProperty = "requestId", keyColumn = "request_id")
    int insertRequest(GroupRequest request);

    @Update("UPDATE GroupRequest SET status = CAST(#{status} AS request_status_enum) WHERE request_id = #{requestId}")
    int updateStatus(@Param("requestId") int requestId, @Param("status") String status);
}
