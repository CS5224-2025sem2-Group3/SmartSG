package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {

    @Select("SELECT user_id, name, email, password_hash FROM \"User\" WHERE email = #{email}")
    @Results(id = "userResultMap", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "passwordHash", column = "password_hash")
    })
    User findByEmail(@Param("email") String email);

    @Insert("INSERT INTO \"User\" (name, email, password_hash) VALUES (#{name}, #{email}, #{passwordHash})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insertUser(User user);

    @Select("SELECT user_id, name, email, password_hash FROM \"User\" WHERE user_id = #{userId}")
    User findById(@Param("userId") Long userId);
}
