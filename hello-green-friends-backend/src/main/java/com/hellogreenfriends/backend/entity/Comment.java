package com.hellogreenfriends.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 评论实体类
 * 用于描述植物的评论信息，包括评论内容、评论者等
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 关系：多个评论属于一个植物
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    // 关系：多个评论由一个用户创建
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // nullable=true:用户可以匿名评论
    private User user;

    // 关系：多个评论可以回复同一个评论
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // 自引用关系

    // 评论的树形结构
    // 关系：一个评论可以有多个回复
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private Set<Comment> replies; // 回复列表

    // 评论内容
    @Lob
    @Column(nullable = false,columnDefinition = "NVARCHAR(MAX)")
    private String content;

    // 评论时间
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        if (this.id == null || comment.id == null) {
            return false;
        }
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}