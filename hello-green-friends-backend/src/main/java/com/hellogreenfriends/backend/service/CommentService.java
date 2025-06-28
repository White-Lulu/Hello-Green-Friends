package com.hellogreenfriends.backend.service;

import com.hellogreenfriends.backend.dto.CommentDTO;
import com.hellogreenfriends.backend.entity.Comment;
import com.hellogreenfriends.backend.entity.Plant;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.exception.ResourceNotFoundException;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.repository.CommentRepository;
import com.hellogreenfriends.backend.repository.PlantRepository;
import com.hellogreenfriends.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CommentService 类用于处理与植物评论相关的业务逻辑。
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PlantRepository plantRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByPlantId(Integer plantId, Pageable pageable) {
        if (!plantRepository.existsById(plantId)) {
            throw new ResourceNotFoundException("Plant not found with id: " + plantId);
        }
        Page<Comment> commentPage = commentRepository.findByPlantIdAndParentCommentIsNullOrderByCreatedAtDesc(plantId, pageable);
        return commentPage.map(EntityMapper::toCommentDTO);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByUserId(Integer userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        Page<Comment> commentPage = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return commentPage.map(EntityMapper::toCommentDTO);
    }

    @Transactional
    public CommentDTO createComment(Integer plantId, Integer userId, String content, Integer parentCommentId) {
        Plant plant = plantRepository.findById(plantId)
            .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + plantId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Comment newComment = new Comment();
        newComment.setPlant(plant);
        newComment.setUser(user);
        newComment.setContent(content);

        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + parentCommentId));
            newComment.setParentComment(parent);
        }
        Comment savedComment = commentRepository.save(newComment);
        return EntityMapper.toCommentDTO(savedComment);
    }
    
    @Transactional
    public void deleteComment(Integer commentId, Integer currentUserId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        if (comment.getUser() == null ||
            (!comment.getUser().getId().equals(currentUserId) && currentUser.getRole() != User.UserRole.ADMIN)) {
            throw new SecurityException("User is not authorized to delete this comment.");
        }

        comment.setContent("此评论已被作者删除");
        comment.setUser(null);
        commentRepository.save(comment);
    }
}