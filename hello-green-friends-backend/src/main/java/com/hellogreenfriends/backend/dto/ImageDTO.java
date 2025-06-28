package com.hellogreenfriends.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImageDTO {
    private Integer id;
    private String imageUrl;
    private String caption;
    private UserDTO uploader;
    private LocalDateTime createdAt;
    private String status;
}