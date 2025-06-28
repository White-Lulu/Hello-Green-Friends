package com.hellogreenfriends.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlantDTO {
    private Integer id;
    private String name;
    private String scientificName;
    private String familyGenus;
    private String mainImageUrl;
    private String floweringPeriod;
    private String description;
    private UserDTO creator;
    private String status;
    private List<TagDTO> tags;
    private List<LocationDTO> locations;
    private List<ImageDTO> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}