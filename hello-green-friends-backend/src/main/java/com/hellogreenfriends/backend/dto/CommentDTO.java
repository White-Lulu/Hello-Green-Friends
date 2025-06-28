package com.hellogreenfriends.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Integer id;
    private String content;
    private UserDTO user;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;
    private Integer parentId;
    private CommentPlantInfoDTO plant;
}