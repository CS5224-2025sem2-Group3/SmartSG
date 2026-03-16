package com.nus.cs5224.smartsg.mapper;

import com.nus.cs5224.smartsg.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {

    @Select("SELECT user_id, name, email, password_hash FROM \"User\" WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    @Insert("INSERT INTO \"User\" (name, email, password_hash) VALUES (#{name}, #{email}, #{passwordHash})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insertUser(User user);
}
