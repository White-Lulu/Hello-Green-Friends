package com.hellogreenfriends.backend.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private String content;
    private Integer parentCommentId;
}