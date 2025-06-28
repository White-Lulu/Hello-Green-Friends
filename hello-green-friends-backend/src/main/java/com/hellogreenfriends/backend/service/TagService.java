package com.hellogreenfriends.backend.service;

import com.hellogreenfriends.backend.entity.Tag;
import com.hellogreenfriends.backend.repository.TagRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TagService 类，用于处理标签相关的业务逻辑。
 * 主要功能包括获取所有标签和根据标签名称查找或创建标签。
 */
@Service
public class TagService {

    private final TagRepository tagRepository;


    // TagService 构造函数，注入 TagRepository
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 获取所有可用的标签列表。
     * 对应 API: GET /api/tags 
     */
    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * 根据标签名称查找或创建标签，供 PlantService 调用
     * @param tagNames 一组标签名称
     * @return 对应的一组 Tag 实体
     */
    @Transactional
    public Set<Tag> findOrCreateTags(Set<String> tagNames) {
        return tagNames.stream()
                // 对每个标签名称进行处理
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(name);
                            return tagRepository.save(newTag);
                        }))
                .collect(Collectors.toSet());
    }
}