package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.dto.PlantCreateDTO;
import com.hellogreenfriends.backend.dto.PlantDTO;
import com.hellogreenfriends.backend.entity.Plant;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.service.PlantService;
import com.hellogreenfriends.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import com.hellogreenfriends.backend.dto.PagedResponseDTO;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlantController {

    @Autowired
    private PlantService plantService;
    @Autowired
    private UserService userService;

    @GetMapping("/plants")
    public ResponseEntity<ApiResponse> getPublishedPlants(
            @RequestParam(required = false) Integer tagId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String areaName,
            Pageable pageable) {
        Page<PlantDTO> plantDtoPage = plantService.getPublishedPlants(tagId, period, areaName, pageable);
        PagedResponseDTO<PlantDTO> response = new PagedResponseDTO<>(
                plantDtoPage.getContent(),
                plantDtoPage.getNumber(),
                plantDtoPage.getTotalPages(),
                plantDtoPage.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/plants/{id}")
    public ResponseEntity<ApiResponse> getPlantById(@PathVariable Integer id) {
        Plant plantEntity = plantService.getPlantById(id);
        PlantDTO plantDTO = EntityMapper.toPlantDTO(plantEntity);
        return ResponseEntity.ok(ApiResponse.success(plantDTO));
    }

    @GetMapping("/my-gallery")
    public ResponseEntity<ApiResponse> getMyPlants(
            @RequestParam(required = false) Plant.PlantStatus status,
            Principal principal,
            Pageable pageable) {
        User currentUser = getCurrentUser(principal);
        Page<PlantDTO> plantDtoPage = plantService.getPlantsByCreator(currentUser.getId(), status, pageable);
        PagedResponseDTO<PlantDTO> response = new PagedResponseDTO<>(
                plantDtoPage.getContent(),
                plantDtoPage.getNumber(),
                plantDtoPage.getTotalPages(),
                plantDtoPage.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/my-gallery")
    public ResponseEntity<ApiResponse> createPlant(
        @RequestPart("plant") PlantCreateDTO plantCreateDTO,
        @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
        Principal principal) {
    User currentUser = getCurrentUser(principal);
    Plant createdPlant = plantService.createPlant(plantCreateDTO, mainImage, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success(EntityMapper.toPlantDTO(createdPlant)));
    }

    @GetMapping("/my-gallery/{id}")
    public ResponseEntity<ApiResponse> getPlantByIdForUser(@PathVariable Integer id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Plant plant = plantService.getPlantByIdAndCreator(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(EntityMapper.toPlantDTO(plant)));
    }

    @PutMapping("/my-gallery/{id}")
    public ResponseEntity<ApiResponse> updatePlant(
            @PathVariable Integer id,
            @RequestPart("plant") PlantCreateDTO plantCreateDTO,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage, // 接收图片
            Principal principal) {
        User currentUser = getCurrentUser(principal);
        Plant updatedPlant = plantService.updatePlant(id, plantCreateDTO, mainImage, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(EntityMapper.toPlantDTO(updatedPlant)));
    }

    @DeleteMapping("/my-gallery/{id}")
    public ResponseEntity<ApiResponse> deletePlant(@PathVariable Integer id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        plantService.deletePlant(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Plant deleted successfully."));
    }

    @GetMapping("/plants/search")
    public ResponseEntity<ApiResponse> searchPlants(
            @RequestParam String query,
            Pageable pageable) {
        Page<PlantDTO> plantDtoPage = plantService.searchPlants(query, pageable);
        PagedResponseDTO<PlantDTO> response = new PagedResponseDTO<>(
                plantDtoPage.getContent(),
                plantDtoPage.getNumber(),
                plantDtoPage.getTotalPages(),
                plantDtoPage.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/plants/map-points")
    public ResponseEntity<ApiResponse> getMapPoints() {
        return ResponseEntity.ok(ApiResponse.success(plantService.getMapPoints()));
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Authentication is required to perform this action.");
        }
        try {
            return userService.findUserByUsername(principal.getName());
        } catch (UsernameNotFoundException e) {
            throw new SecurityException("User not found, authentication failed.");
        }
    }
}