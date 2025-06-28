package com.hellogreenfriends.backend.mapper;

import com.hellogreenfriends.backend.dto.*;
import com.hellogreenfriends.backend.entity.*;
import java.util.stream.Collectors;
import java.util.Collections;

public class EntityMapper {

    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername()); 
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole().name());
        return dto;
    }

    public static LocationDTO toLocationDTO(PlantLocation plantLocation) {
        if (plantLocation == null || plantLocation.getLocation() == null) {
            return null;
        }
        LocationDTO dto = new LocationDTO();
        Location location = plantLocation.getLocation();
        
        dto.setId(location.getId());
        dto.setAreaName(location.getAreaName());
        dto.setSpecificName(location.getSpecificName());
        dto.setMapCoordX(location.getMapCoordX());
        dto.setMapCoordY(location.getMapCoordY());
        dto.setNotes(plantLocation.getNotes());
        
        return dto;
    }

    public static PlantDTO toPlantDTO(Plant plant) {
        if (plant == null) {
            return null;
        }
        PlantDTO dto = new PlantDTO();
        dto.setId(plant.getId());
        dto.setName(plant.getName());
        dto.setScientificName(plant.getScientificName());
        dto.setFamilyGenus(plant.getFamilyGenus());
        dto.setMainImageUrl(plant.getMainImageUrl());
        dto.setFloweringPeriod(plant.getFloweringPeriod());
        dto.setDescription(plant.getDescription());
        dto.setStatus(plant.getStatus() != null ? plant.getStatus().name() : null);
        dto.setCreatedAt(plant.getCreatedAt());
        dto.setUpdatedAt(plant.getUpdatedAt());
        dto.setCreator(toUserDTO(plant.getCreator()));

        if (plant.getTags() != null) {
            dto.setTags(plant.getTags().stream()
                .map(EntityMapper::toTagDTO)
                .collect(Collectors.toList()));
        } else {
            dto.setTags(Collections.emptyList());
        }

        if (plant.getPlantLocations() != null) {
            dto.setLocations(plant.getPlantLocations().stream()
                .map(EntityMapper::toLocationDTO)
                .collect(Collectors.toList()));
        } else {
            dto.setLocations(Collections.emptyList());
        }
        if (plant.getImages() != null) {
            dto.setImages(plant.getImages().stream()
                .map(EntityMapper::toImageDTO)
                .collect(Collectors.toList()));
        } else {
            dto.setImages(Collections.emptyList());
        }

        return dto;
    }

    public static CommentPlantInfoDTO toCommentPlantInfoDTO(Plant plant) {
        if (plant == null) {
            return null;
        }
        CommentPlantInfoDTO dto = new CommentPlantInfoDTO();
        dto.setId(plant.getId());
        dto.setName(plant.getName());
        dto.setMainImageUrl(plant.getMainImageUrl());
        return dto;
    }

    public static CommentDTO toCommentDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUser(toUserDTO(comment.getUser())); // 如果 user 为 null (已软删除), 此方法会返回 null
        dto.setPlant(toCommentPlantInfoDTO(comment.getPlant()));

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(EntityMapper::toCommentDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }


    public static TagDTO toTagDTO(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        return dto;
    }
    
    public static ImageDTO toImageDTO(Image image) {
    if (image == null) {
        return null;
    }
    ImageDTO dto = new ImageDTO();
    dto.setId(image.getId());
    dto.setImageUrl(image.getImageUrl());
    dto.setCaption(image.getCaption());
    dto.setCreatedAt(image.getCreatedAt());
    dto.setUploader(toUserDTO(image.getUploader()));
    if (image.getStatus() != null) {
            dto.setStatus(image.getStatus().name());
        }
    return dto;
    }

}