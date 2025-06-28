package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 用于处理标签相关的数据库操作。
 * 主要功能是根据标签名称查找标签，以确保标签的唯一性。
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    /**
     * 根据标签名称精确查找标签。
     * @param name 标签名称
     * @return 包含标签实体的 Optional 对象
     */
    Optional<Tag> findByName(String name);
}
