package com.hellogreenfriends.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class PlantCreateDTO {

    private String name;
    private String scientificName;
    private String description;
    private String floweringPeriod;
    private String status;
    private Set<Long> tagIds; 
    private List<LocationInfo> locations;

    @Data
    public static class LocationInfo {
        private String areaName;
        private String specificName;
        private Float mapCoordX;
        private Float mapCoordY;
    }
}