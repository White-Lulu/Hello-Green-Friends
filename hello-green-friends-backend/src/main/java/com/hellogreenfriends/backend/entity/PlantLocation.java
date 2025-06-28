package com.hellogreenfriends.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 植物与地点的关联实体类
 */
@Entity
// 
@Table(name = "plant_locations", uniqueConstraints = {
    // 确保每个植物在每个地点只能有一条记录
    @UniqueConstraint(columnNames = {"plant_id", "location_id"})
})
@Getter
@Setter
public class PlantLocation {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 关系：一个植物可以分布在多个地点
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    // 关系：一个地点可以有多种植物
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // 关联的补充信息
    @Column(name = "notes", nullable = true,columnDefinition = "NVARCHAR(255)")
    private String notes; // 如：此处有3棵

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlantLocation that = (PlantLocation) o;
        if (id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}