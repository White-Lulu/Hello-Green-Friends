package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.dto.PlantDTO;
import com.hellogreenfriends.backend.dto.PlantStatusUpdateDTO;
import com.hellogreenfriends.backend.entity.Plant;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.service.PlantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hellogreenfriends.backend.dto.PagedResponseDTO;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private PlantService plantService;

    @GetMapping("/plants/pending")
    public ResponseEntity<ApiResponse> getPendingPlants(Pageable pageable) {
        Page<PlantDTO> dtoPage = plantService.getPendingPlants(pageable);
        PagedResponseDTO<PlantDTO> response = new PagedResponseDTO<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/plants/{id}/status")
    public ResponseEntity<ApiResponse> reviewPlant(
            @PathVariable Integer id,
            @RequestBody PlantStatusUpdateDTO dto) {
        Plant reviewedPlant = plantService.reviewPlant(id, dto);
        return ResponseEntity.ok(ApiResponse.success(EntityMapper.toPlantDTO(reviewedPlant)));
    }
}