package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String email;
}