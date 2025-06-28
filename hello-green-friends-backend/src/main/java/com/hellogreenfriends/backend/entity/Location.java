package com.hellogreenfriends.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * 地点实体类
 * 用于描述植物分布的具体地点
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
public class Location {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 区域名称，如：东区
    @Column(name = "area_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String areaName;

    // 具体地名，如：图书馆前
    @Column(name = "specific_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String specificName;

    // 在地图图片上的X坐标
    @Column(name = "map_coord_x")
    private Float mapCoordX; 

    // 在地图图片上的Y坐标
    @Column(name = "map_coord_y")
    private Float mapCoordY;

    // 地点描述
    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // 关系：一个地点可以有多个植物分布记录
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlantLocation> plantLocations;

    // 创建时间
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 更新时间
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 重写 equals 和 hashCode 方法，只使用 id 字段来判断实体是否相等
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        if (id == null || location.id == null) {
            return false;
        }
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        // 只使用 id 计算哈希值
        // 如果 id 是 null（新实体），则使用 Object 的默认哈希值
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}