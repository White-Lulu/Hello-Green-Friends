package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String nickname;
    private String avatarUrl;
    private String username; 
    private String email;  
    private String role;
}