package com.hellogreenfriends.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户实体类
 * 用于描述系统中的用户信息，包括登录名、昵称、密码等
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 用户名，唯一且不能为空
    @Column(unique = true, nullable = false,columnDefinition = "NVARCHAR(255)")
    private String username; 

    // 昵称，可重复，不能为空
    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    private String nickname; 

    // 密码哈希，已加密，不能为空
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 头像图片URL，可为空
    @Column(name = "avatar_url")
    private String avatarUrl; // 头像图片URL

    // 邮箱，唯一且不能为空
    @Column(unique = true)
    private String email;

    // 用户角色
    @Enumerated(EnumType.STRING) // 将枚举类型映射为字符串
    @Column(nullable = false)
    private UserRole role = UserRole.USER; // 默认为 USER

    // 用户状态
    @Enumerated(EnumType.STRING) // 将枚举类型映射为字符串
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE; // 默认为 ACTIVE

    // 关系：一个用户可以创建多个植物条目
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Plant> createdPlants;

    // 创建时间
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 用户角色枚举
    public enum UserRole {
        USER,
        ADMIN
    }

    // 用户状态枚举
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        BANNED
    }

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (id == null || user.id == null) {
            return false;
        }
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}