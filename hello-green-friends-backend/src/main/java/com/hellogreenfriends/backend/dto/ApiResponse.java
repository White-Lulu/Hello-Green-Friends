package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(true, "Operation successful", data);
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }
    
    public static ApiResponse failure(String message) {
        return new ApiResponse(false, message, null);
    }
}