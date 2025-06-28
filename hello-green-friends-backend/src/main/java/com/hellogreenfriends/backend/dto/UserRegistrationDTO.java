package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String username;
    private String nickname;
    private String email;
    private String password;
}