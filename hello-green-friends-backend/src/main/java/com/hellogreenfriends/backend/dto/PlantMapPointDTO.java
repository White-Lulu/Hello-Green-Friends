package com.hellogreenfriends.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantMapPointDTO {
    private Integer plantId;
    private String name;
    private String scientificName;
    private List<TagDTO> tags;
    private String mainImageUrl;
    private Float mapCoordX;
    private Float mapCoordY;
}