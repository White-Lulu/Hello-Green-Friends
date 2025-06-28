package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 用于处理地点相关的数据库操作。
 * 主要功能是获取所有不重复的区域名称和根据区域名称及具体地点名称查找地点。
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    @Query("SELECT DISTINCT l.areaName FROM Location l ORDER BY l.areaName")
    List<String> findDistinctAreaNames();

    Optional<Location> findByAreaNameAndSpecificName(String areaName, String specificName);
}