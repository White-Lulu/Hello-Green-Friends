package com.hellogreenfriends.backend.service;

import com.hellogreenfriends.backend.dto.PlantCreateDTO;
import com.hellogreenfriends.backend.dto.PlantDTO;
import com.hellogreenfriends.backend.dto.PlantMapPointDTO;
import com.hellogreenfriends.backend.dto.PlantStatusUpdateDTO;
import com.hellogreenfriends.backend.entity.Location;
import com.hellogreenfriends.backend.entity.Plant;
import com.hellogreenfriends.backend.entity.PlantLocation;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.entity.Tag;
import com.hellogreenfriends.backend.exception.ResourceNotFoundException;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PlantService 类处理植物相关的业务逻辑
 * 包括创建、更新、删除植物信息，以及获取植物列表和详情
 */
@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final LocationRepository locationRepository;
    private final PlantLocationRepository plantLocationRepository;
    private final ImageService imageService;

    /**
     * 创建新的植物实体
     * @param dto 包含植物信息的 DTO
     * @param mainImage 植物的主图文件
     * @param creatorId 创建者的用户 ID
     * @return 保存后的植物实体
     */ 
    @Transactional
    public Plant createPlant(PlantCreateDTO dto, MultipartFile mainImage, Integer creatorId) {
        
        // 检查创建者是否存在
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));
        Plant newPlant = new Plant();

        // 如果 mainImage 不为空，则调用 ImageService 的新方法来存储图片并获取 URL
        if (mainImage != null && !mainImage.isEmpty()) {
            String imageUrl = imageService.storeAndGetUrl(mainImage, "plants");
            newPlant.setMainImageUrl(imageUrl);
        }

        // 设置植物的基本信息
        newPlant.setName(dto.getName());
        newPlant.setScientificName(dto.getScientificName());
        newPlant.setDescription(dto.getDescription());
        newPlant.setFloweringPeriod(dto.getFloweringPeriod());
        newPlant.setCreator(creator);

        // 将状态字符串转换为枚举 没有传入状态时，默认为 DRAFT
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            newPlant.setStatus(Plant.PlantStatus.valueOf(dto.getStatus()));
        } else {
            newPlant.setStatus(Plant.PlantStatus.DRAFT);
        }
        // 将标签转换为实体并设置
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = tagRepository.findAllById(dto.getTagIds().stream().map(Long::intValue).collect(Collectors.toList()))
                    .stream().collect(Collectors.toSet());
            newPlant.setTags(tags);
        }

        // 保存新植物实体
        Plant savedPlant = plantRepository.save(newPlant);

        //  创建或更新植物位置关联
        createOrUpdatePlantLocations(dto.getLocations(), savedPlant);

        return savedPlant; // 返回实体
    }

    /**
     * 更新植物信息,传入信息同上
     * @param plantId 植物ID
     * @param dto 包含植物信息的 DTO
     * @param mainImage 植物的主图文件
     * @param userId 当前用户的 ID
     * @return 更新后的植物实体
     */
    @Transactional
    public Plant updatePlant(Integer plantId, PlantCreateDTO dto, MultipartFile mainImage, Integer userId) {
        // 根据 plantId 获取现有植物实体
        Plant existingPlant = getPlantById(plantId);

        // 检查当前用户是否是植物的创建者
        if (!existingPlant.getCreator().getId().equals(userId)) {
            throw new SecurityException("User is not authorized to edit this plant.");
        }

        //  如果 mainImage 不为空
        if (mainImage != null && !mainImage.isEmpty()) {
            // 调用 ImageService 的方法来存储图片并获取 URL
            String imageUrl = imageService.storeAndGetUrl(mainImage, "plants");
            existingPlant.setMainImageUrl(imageUrl); // 更新主图 URL
        }

        // 更新植物的各个基本信息
        existingPlant.setName(dto.getName());
        existingPlant.setScientificName(dto.getScientificName());
        existingPlant.setDescription(dto.getDescription());
        existingPlant.setFloweringPeriod(dto.getFloweringPeriod());

        // 将 status 字符串转换为枚举
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            existingPlant.setStatus(Plant.PlantStatus.valueOf(dto.getStatus()));
        }

        // 将标签转换为实体并设置
        if (dto.getTagIds() != null) {
            Set<Tag> tags = tagRepository.findAllById(dto.getTagIds().stream().map(Long::intValue).collect(Collectors.toList()))
                    .stream().collect(Collectors.toSet());
            existingPlant.setTags(tags);
        }

        // 先通过 JPA 从数据库删除旧的关联记录
        plantLocationRepository.deleteByPlantId(plantId);
        
        // 如果现有植物实体有旧的位置信息，先清空
        // 防止 Hibernate 在事务提交时尝试持久化已经删除的旧数据
        if (existingPlant.getPlantLocations() != null) {
            existingPlant.getPlantLocations().clear();
        }

        // 将删除操作刷新到数据库，确保后续操作的隔离性
        plantLocationRepository.flush();

        // 基于前端传来的新数据，（安全地）创建新的位置关联
        createOrUpdatePlantLocations(dto.getLocations(), existingPlant);

        return plantRepository.save(existingPlant); // 返回实体
    }

    /**
     * 更新植物地点
     * @param plantId 植物ID
     * @param locationUpdateDTO 包含新地点的 DTO
     * @return 更新后的植物实体
     */
    private void createOrUpdatePlantLocations(List<PlantCreateDTO.LocationInfo> locationInfos, Plant plant) {
        if (locationInfos == null || locationInfos.isEmpty()) {
            return;
        }
        // 遍历每个地点信息
        // 如果没有传入地点信息，则不进行任何操作
        for (PlantCreateDTO.LocationInfo locInfo : locationInfos) {
            Location location = locationRepository
                    .findByAreaNameAndSpecificName(locInfo.getAreaName(), locInfo.getSpecificName())
                    .orElseGet(() -> {
                        Location newLoc = new Location();
                        newLoc.setAreaName(locInfo.getAreaName());
                        newLoc.setSpecificName(locInfo.getSpecificName());
                        newLoc.setMapCoordX(locInfo.getMapCoordX());
                        newLoc.setMapCoordY(locInfo.getMapCoordY());
                        return locationRepository.save(newLoc);
                    });

            PlantLocation plantLocation = new PlantLocation();
            plantLocation.setPlant(plant);
            plantLocation.setLocation(location);
            plantLocationRepository.save(plantLocation);
        }
    }

    /**
     * 删除植物实体
     * @param plantId 植物ID
     * @param creatorId 创建者的用户ID
     */
    @Transactional
    public void deletePlant(Integer plantId, Integer creatorId) {
        Plant plant = getPlantById(plantId);

        // 检查当前用户是否是植物的创建者
        if (!plant.getCreator().getId().equals(creatorId)) {
            throw new SecurityException("User is not authorized to delete this plant.");
        }

        // 删除与植物相关的位置信息
        plantLocationRepository.deleteByPlantId(plantId);

        // 删除植物实体
        plantRepository.delete(plant);
    }

    /**
     * 获取植物详情
     * @param id 植物ID
     * @return 植物实体
     */
    @Transactional(readOnly = true)
    public Plant getPlantById(Integer id) {
        return plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + id));
    }

    /**
     * 获取植物列表
     * @param pageable 分页信息
     * @return 植物实体的分页结果
     */
    @Transactional(readOnly = true)
    public Page<PlantDTO> getPublishedPlants(Integer tagId, String period, String areaName, Pageable pageable) {
        return plantRepository.findWithFilters(tagId, period, areaName, pageable)
                .map(EntityMapper::toPlantDTO);
    }

    /**
     * 根据植物ID和创建者ID获取植物详情
     * @param plantId 植物ID
     * @param creatorId 创建者的用户ID
     * @param pageable 分页信息
     * @return 植物实体
     */
    @Transactional(readOnly = true)
    public Page<PlantDTO> getPlantsByCreator(Integer creatorId, Plant.PlantStatus status, Pageable pageable) {
        Page<Plant> plantPage;
        if (status != null) {
            plantPage = plantRepository.findByCreatorIdAndStatus(creatorId, status, pageable);
        } else {
            plantPage = plantRepository.findByCreatorId(creatorId, pageable);
        }
        return plantPage.map(EntityMapper::toPlantDTO);
    }
    
    /**
     * 获取地图上的植物点
     * @return 包含植物位置信息的 DTO 列表
     */
    @Transactional(readOnly = true)
    public List<PlantMapPointDTO> getMapPoints() {
        // 获取包含所有需要数据的 Plant 实体列表
        List<Plant> plants = plantRepository.findPublishedPlantsForMap();

        // 在服务层中进行手动映射，将实体转换为 DTO
        // 使用 flatMap 是因为一个植物可能分布在多个地点，每个地点都应成为地图上的一个点
        return plants.stream()
                .flatMap(plant -> plant.getPlantLocations().stream().map(plantLocation -> {
                    Location location = plantLocation.getLocation();
                    PlantMapPointDTO dto = new PlantMapPointDTO();
                    // 映射植物信息
                    dto.setPlantId(plant.getId());
                    dto.setName(plant.getName());
                    dto.setScientificName(plant.getScientificName());
                    dto.setMainImageUrl(plant.getMainImageUrl());
                    dto.setMapCoordX(location.getMapCoordX());
                    dto.setMapCoordY(location.getMapCoordY());

                    // 映射标签信息
                    if (plant.getTags() != null) {
                        dto.setTags(plant.getTags().stream()
                                .map(EntityMapper::toTagDTO)
                                .collect(Collectors.toList()));
                    } else {
                        dto.setTags(Collections.emptyList());
                    }
                    return dto;
                }))
                .collect(Collectors.toList());
    }

    /*
     *  根据植物名称进行模糊搜索
     */
     @Transactional(readOnly = true)
    public Page<PlantDTO> searchPlants(String query, Pageable pageable) {
        Page<Plant> plantPage = plantRepository.findByNameContainingIgnoreCaseAndStatus(query, Plant.PlantStatus.PUBLISHED, pageable);
        return plantPage.map(EntityMapper::toPlantDTO);
    }

    /**
     * 根据植物ID和创建者ID获取植物详情
     * @param plantId 植物ID
     * @param creatorId 创建者的用户ID
     * @return 植物实体
     */
    @Transactional(readOnly = true)
    public Plant getPlantByIdAndCreator(Integer plantId, Integer creatorId) {
        return plantRepository.findByIdAndCreatorId(plantId, creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + plantId + " or you are not the creator."));
    }

    /**
     * 获取所有待审核的植物列表
     * 对应 API: GET /api/admin/plants/pending
     */
    @Transactional(readOnly = true)
    public Page<PlantDTO> getPendingPlants(Pageable pageable) {
        Page<Plant> plantPage = plantRepository.findByStatus(Plant.PlantStatus.PENDING_REVIEW, pageable);
        return plantPage.map(EntityMapper::toPlantDTO);
    }

    /**
     * 审核一个植物（通过或拒绝）
     * 对应 API: PUT /api/admin/plants/{id}/status
     */
    @Transactional
    public Plant reviewPlant(Integer plantId, PlantStatusUpdateDTO dto) {
        Plant plant = getPlantById(plantId);

        // 只有处于“待审核”状态的植物才能被审核
        if (plant.getStatus() != Plant.PlantStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Only plants with PENDING_REVIEW status can be reviewed.");
        }

        // 根据审核结果更新植物状态
        plant.setStatus(dto.getStatus());
        // 如果是拒绝状态，设置审核备注
        plant.setReviewNotes(dto.getReviewNotes());
        
        // 返回更新后的植物实体
        return plantRepository.save(plant);
    }
}