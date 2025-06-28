package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.PlantLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用于处理植物与地点关联的数据库操作。
 * 主要功能是通过植物ID删除所有关联记录。
 */
@Repository
public interface PlantLocationRepository extends JpaRepository<PlantLocation, Long> {
    void deleteByPlantId(Integer plantId);
}