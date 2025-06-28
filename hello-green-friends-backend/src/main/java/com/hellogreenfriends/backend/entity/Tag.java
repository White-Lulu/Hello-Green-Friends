package com.hellogreenfriends.backend.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.Objects;
import java.util.HashSet;
import jakarta.persistence.*;
import java.util.Set;

/**
 * 标签实体类
 * 用于描述植物的标签信息
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
public class Tag {

    @Id // 主键为 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 标签名
    @Column(unique = true, nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name; 

    // 标签描述
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // 关系：一个标签可以关联多个植物
    @ManyToMany(mappedBy = "tags")
    private Set<Plant> plants = new HashSet<>(); // 初始化植物集合

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        if (this.id == null || tag.id == null) {
            return false;
        }
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}