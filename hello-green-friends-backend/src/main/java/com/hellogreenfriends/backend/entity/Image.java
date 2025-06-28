package com.hellogreenfriends.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 图片实体类
 * 用于描述植物的图片信息,包括上传者、图片URL、图片说明等
 * 用于用户头像设置
 */
@Entity
@Table(name = "images")
@Getter
@Setter
public class Image {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 关系：多张图片属于一个植物
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    // 关系：多张图片由一个用户上传
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    // 图片URL
    @Column(name = "image_url", nullable = false)
    private String imageUrl; 

    // 图片说明
    @Column(name = "caption", columnDefinition = "NVARCHAR(255)")
    private String caption; 

    // 图片审核状态
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageStatus status = ImageStatus.PENDING_REVIEW;

    // 创建时间
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 图片状态枚举
    public enum ImageStatus {
        PENDING_REVIEW,
        APPROVED,
        REJECTED
    }

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        if (id == null || image.id == null) {
            return false;
        }
        return Objects.equals(id, image.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}