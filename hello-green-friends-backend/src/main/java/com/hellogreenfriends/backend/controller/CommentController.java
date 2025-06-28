package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.dto.CommentCreateDTO; // 假设已创建
import com.hellogreenfriends.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService; 
    
    @GetMapping("/plants/{plantId}/comments")
    public ResponseEntity<ApiResponse> getCommentsByPlantId(@PathVariable Integer plantId, Pageable pageable) { // 
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByPlantId(plantId, pageable)));
    }

    @GetMapping("/my-comments")
    public ResponseEntity<ApiResponse> getMyComments(Principal principal, Pageable pageable) { // 
        User currentUser = getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByUserId(currentUser.getId(), pageable)));
    }

    @PostMapping("/plants/{plantId}/comments")
    public ResponseEntity<ApiResponse> createComment(@PathVariable Integer plantId, @RequestBody CommentCreateDTO dto, Principal principal) { // 
        User currentUser = getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success(commentService.createComment(plantId, currentUser.getId(), dto.getContent(), dto.getParentCommentId())));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Integer id, Principal principal) { // 
        User currentUser = getCurrentUser(principal);
        commentService.deleteComment(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully."));
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