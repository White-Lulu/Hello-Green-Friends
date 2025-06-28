package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}