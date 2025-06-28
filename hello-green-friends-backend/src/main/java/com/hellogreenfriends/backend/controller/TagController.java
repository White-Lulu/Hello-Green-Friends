package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hellogreenfriends.backend.dto.TagDTO;
import com.hellogreenfriends.backend.mapper.EntityMapper;
import com.hellogreenfriends.backend.entity.Tag;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagDTO> tagDTOs = tags.stream()
                                .map(EntityMapper::toTagDTO)
                                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(tagDTOs));
    }
}