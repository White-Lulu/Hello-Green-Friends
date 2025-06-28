package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用于处理图片相关的数据库操作。
 * 主要功能是根据植物ID查找所有图片。
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByPlantId(Integer plantId);
}