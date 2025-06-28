package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.dto.ImageDTO;
import com.hellogreenfriends.backend.entity.Image;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.service.ImageService;
import com.hellogreenfriends.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @PostMapping("/plants/{plantId}/images")
    public ResponseEntity<ApiResponse> uploadImage(
            @PathVariable Integer plantId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        User currentUser = getCurrentUser(principal);
        Image savedImage = imageService.storeFile(file, plantId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(EntityMapper.toImageDTO(savedImage)));
    }

    @PostMapping("/plants/{plantId}/additional-images")
    public ResponseEntity<ApiResponse> addAdditionalImage(
            @PathVariable Integer plantId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        User currentUser = getCurrentUser(principal);
        Image newImage = imageService.addPlantImage(plantId, currentUser, file);
        return ResponseEntity.ok(ApiResponse.success(EntityMapper.toImageDTO(newImage)));
    }

    @GetMapping("/plants/{plantId}/images")
    public ResponseEntity<ApiResponse> getImages(@PathVariable Integer plantId) {
        List<Image> images = imageService.getImagesByPlantId(plantId);
        List<ImageDTO> imageDTOs = images.stream()
                                        .map(EntityMapper::toImageDTO)
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(imageDTOs));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Integer imageId, Principal principal) {
        User currentUser = getCurrentUser(principal);
        imageService.deleteImage(imageId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully."));
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