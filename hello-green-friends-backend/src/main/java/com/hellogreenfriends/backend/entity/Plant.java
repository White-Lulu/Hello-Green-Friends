package com.hellogreenfriends.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * 植物实体类
 */
@Entity
@Table(name = "plants")
@Getter
@Setter
public class Plant {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增策略
    private Integer id;

    // 植物名称
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)") // 使用 NVARCHAR 以支持多语言字符集
    private String name;

    // 植物学名
    @Column(name = "scientific_name", columnDefinition = "NVARCHAR(255)")
    private String scientificName;

    // 植物科属
    @Column(name = "family_genus", columnDefinition = "NVARCHAR(255)")
    private String familyGenus;

    // 植物花期
    @Column(name = "flowering_period", columnDefinition = "NVARCHAR(255)")
    private String floweringPeriod;

    // 植物简介
    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // 植物主图链接
    @Column(name = "main_image_url")
    private String mainImageUrl;

    // 关系：一个植物由一个用户创建，一个用户可以创建多个植物
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id") // 外键列 
    private User creator;

    // 植物状态
    @Enumerated(EnumType.STRING) // 使用枚举类型
    @Column(length = 50) // 保留一个标准的 @Column 注解来定义长度
    private PlantStatus status = Plant.PlantStatus.DRAFT; // 直接在Java代码中设置默认值

    // 植物审核备注
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    // 关系：一个植物可以有多张图片
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>(); // 初始化照片列表

    // 关系：一个植物可以出现在多个地点（通过 PlantLocation 关联）
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlantLocation> plantLocations = new HashSet<>(); // 初始化地点列表

    // 关系：多个植物可以有多个标签
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "plant_tags", // 关联表名
        joinColumns = @JoinColumn(name = "plant_id"), //  关联的植物外键列
        inverseJoinColumns = @JoinColumn(name = "tag_id") // 关联的标签外键列
    )
    private Set<Tag> tags = new HashSet<>(); // 初始化标签列表

    // 创建时间，自动设置且不可更新
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 更新时间戳
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 植物状态枚举
     * DRAFT: 草稿状态
     * PENDING_REVIEW: 待审核状态
     * PUBLISHED: 已发布状态
     * REJECTED: 已拒绝状态
     * ARCHIVED: 已归档状态
     */
    public enum PlantStatus {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        REJECTED,
        ARCHIVED
    }

    // 重写 equals 和 hashCode 方法，确保基于 id 进行比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plant plant = (Plant) o;
        // 对于新创建（id为null）的实体，它们只有在是同一个对象实例时才相等
        if (this.id == null || plant.id == null) {
            return false;
        }
        return Objects.equals(id, plant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}