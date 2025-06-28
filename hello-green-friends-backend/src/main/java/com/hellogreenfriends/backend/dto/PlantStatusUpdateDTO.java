package com.hellogreenfriends.backend.dto;

import com.hellogreenfriends.backend.entity.Plant;
import lombok.Data;

@Data
public class PlantStatusUpdateDTO {
    private Plant.PlantStatus status; // 'PUBLISHED' 或 'REJECTED'
    private String reviewNotes;
}