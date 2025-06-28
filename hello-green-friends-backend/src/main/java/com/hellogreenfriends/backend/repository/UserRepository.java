package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 用于处理用户相关的数据库操作。
 * 主要功能是根据用户名和邮箱查找用户，以及检查用户名和邮箱是否存在。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}