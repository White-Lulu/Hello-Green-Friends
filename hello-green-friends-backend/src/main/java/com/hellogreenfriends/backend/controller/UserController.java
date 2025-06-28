package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.dto.PasswordChangeDTO;
import com.hellogreenfriends.backend.dto.UserDTO;
import com.hellogreenfriends.backend.dto.UserProfileUpdateDTO;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUserInfo(Principal principal) {
        User currentUser = getCurrentUser(principal);
        UserDTO userDTO = EntityMapper.toUserDTO(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateUserProfile(@RequestBody UserProfileUpdateDTO profileUpdateDTO, Principal principal) {
        User currentUser = getCurrentUser(principal);
        User updatedUser = userService.updateUserProfile(currentUser.getId(), profileUpdateDTO);
        UserDTO userDTO = EntityMapper.toUserDTO(updatedUser);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO, Principal principal) {
        User currentUser = getCurrentUser(principal);
        userService.changePassword(currentUser.getId(), passwordChangeDTO.getOldPassword(), passwordChangeDTO.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<ApiResponse> updateAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        User currentUser = getCurrentUser(principal);
        User updatedUser = userService.updateAvatar(currentUser.getId(), file);
        return ResponseEntity.ok(ApiResponse.success(EntityMapper.toUserDTO(updatedUser)));
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