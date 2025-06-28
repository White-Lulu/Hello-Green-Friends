package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private Integer id;
    private String areaName;
    private String specificName;
    private String notes;
    private Float mapCoordX;
    private Float mapCoordY;
}