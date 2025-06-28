package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.Plant;
import com.hellogreenfriends.backend.entity.Plant.PlantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Integer> {

    /**
     * 复杂动态查询，用于植物图鉴页的筛选功能
     * @param tagId 标签ID (可选)
     * @param period 花期关键词 (可选)
     * @param areaName 区域名称 (可选)
     * @param pageable 分页排序信息
     * @return 符合条件的植物分页结果
     */
    @Query(value = "SELECT DISTINCT p FROM Plant p " +
                   "LEFT JOIN FETCH p.tags " +
                   "LEFT JOIN FETCH p.plantLocations pl " +
                   "LEFT JOIN FETCH pl.location l " +
                   "WHERE p.status = 'PUBLISHED' " +
                   "AND (:tagId IS NULL OR EXISTS (SELECT t FROM p.tags t WHERE t.id = :tagId)) " +
                   "AND (:period IS NULL OR p.floweringPeriod LIKE %:period%) " +
                   "AND (:areaName IS NULL OR pl.location.areaName = :areaName)",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Plant p " +
                        "LEFT JOIN p.tags t_count " +
                        "LEFT JOIN p.plantLocations pl_count " +
                        "WHERE p.status = 'PUBLISHED' " +
                        "AND (:tagId IS NULL OR t_count.id = :tagId) " +
                        "AND (:period IS NULL OR p.floweringPeriod LIKE %:period%) " +
                        "AND (:areaName IS NULL OR pl_count.location.areaName = :areaName)")
    Page<Plant> findWithFilters(
        @Param("tagId") Integer tagId,
        @Param("period") String period,
        @Param("areaName") String areaName,
        Pageable pageable
    );

    /**
     * 根据植物名或别名进行模糊搜索
     * @param nameQuery 搜索关键词
     * @param pageable 分页信息
     * @return 搜索结果
     */
    Page<Plant> findByNameContainingIgnoreCaseAndStatus(String nameQuery, PlantStatus status, Pageable pageable);
 
    /**
     * 根据地点的区域名或具体地名模糊搜索植物。
     * @param locationQuery 搜索关键词
     * @param pageable 分页信息
     * @return 搜索结果
     */
    @Query("SELECT DISTINCT p FROM Plant p " +
           "JOIN p.plantLocations pl JOIN pl.location l " +
           "WHERE (l.areaName LIKE %:locationQuery% OR l.specificName LIKE %:locationQuery%) " +
           "AND p.status = 'PUBLISHED'")
    Page<Plant> findByLocationName(@Param("locationQuery") String locationQuery, Pageable pageable);

    /**
     * 根据创建者ID和状态查找植物。
     * @param creatorId 创建者用户ID
     * @param status 植物状态 (可选)
     * @param pageable 分页信息
     * @return 结果
     */
    Page<Plant> findByCreatorIdAndStatus(Integer creatorId, PlantStatus status, Pageable pageable);
    Page<Plant> findByCreatorId(Integer creatorId, Pageable pageable);

    /**
     * 根据状态查找植物，用于后台管理获取待审核列表等。
     * @param status 植物状态
     * @param pageable 分页信息
     * @return 结果
     */
    
    Page<Plant> findByStatus(Plant.PlantStatus status, Pageable pageable);

    /**
     * 查询出完整的 Plant 实体。
     * 使用 LEFT JOIN FETCH 急切加载关联的 plantLocations、location 和 tags，
     * DISTINCT 关键字确保每个植物只返回一次。
     */
    @Query("SELECT DISTINCT p FROM Plant p " +
           "LEFT JOIN FETCH p.plantLocations pl " +
           "LEFT JOIN FETCH pl.location l " +
           "LEFT JOIN FETCH p.tags t " +
           "WHERE p.status = 'PUBLISHED' AND pl.location IS NOT NULL")
    List<Plant> findPublishedPlantsForMap();

    /**
     * 根据ID和创建者ID查找植物。
     * 确保用户只能操作自己创建的植物
     */
    Optional<Plant> findByIdAndCreatorId(Integer id, Integer creatorId);

    /**
     * 根据ID和状态查找植物。
     * 用于获取单个“已发布”的植物详情
     */
    Optional<Plant> findByIdAndStatus(Integer id, Plant.PlantStatus status);

}