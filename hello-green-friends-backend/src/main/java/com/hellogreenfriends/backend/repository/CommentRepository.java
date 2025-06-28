package com.hellogreenfriends.backend.repository;

import com.hellogreenfriends.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 处理评论相关的数据库操作。
 * 主要功能是根据植物ID查找顶级评论和根据用户ID查找所有评论。
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @EntityGraph(attributePaths = {"user", "replies", "replies.user"})
    Page<Comment> findByPlantIdAndParentCommentIsNullOrderByCreatedAtDesc(Integer plantId, Pageable pageable);

    @EntityGraph(attributePaths = {"plant"})
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
}