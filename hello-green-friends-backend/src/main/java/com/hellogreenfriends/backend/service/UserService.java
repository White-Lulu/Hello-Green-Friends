package com.hellogreenfriends.backend.service;

import com.hellogreenfriends.backend.dto.UserProfileUpdateDTO;
import com.hellogreenfriends.backend.dto.UserRegistrationDTO;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.exception.ResourceNotFoundException;
import com.hellogreenfriends.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * UserService 类，用于处理用户相关的业务逻辑
 * 主要功能包括用户注册、查找用户、更新用户信息和头像等。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private ImageService imageService;

    /**
     * 注册新用户。
     * 对应 API: POST /api/auth/register 
     */
    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        // 创建新用户实体并设置属性
        User newUser = new User();
        newUser.setUsername(registrationDTO.getUsername());
        newUser.setNickname(registrationDTO.getNickname());
        newUser.setEmail(registrationDTO.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(User.UserRole.USER);
        newUser.setStatus(User.UserStatus.ACTIVE);

        // 保存新用户到数据库
        return userRepository.save(newUser);
    }
    
    /**
     * 根据用户ID查找用户。
     */
    @Transactional(readOnly = true)
    public User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    /**
     * 更新用户昵称等基本信息。
     */
    @Transactional
    public User updateUserProfile(Integer userId, String newNickname) {
        User user = findUserById(userId);
        user.setNickname(newNickname);
        return userRepository.save(user);
    }

    /**
     * 修改用户密码。
     */
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = findUserById(userId);
        
        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

    }

    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public User updateAvatar(Integer userId, MultipartFile file) {
        User user = findUserById(userId);

        // 1. 调用ImageService保存头像图片到avatars子目录
        String newFileName = imageService.saveFile(file, "avatars");

        // 2. 构建头像的可访问URL
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/avatars/")
                .path(newFileName)
                .toUriString();

        // 3. 更新用户的avatarUrl字段并保存
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    public User updateUserProfile(Integer userId, UserProfileUpdateDTO profileUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 检查 DTO 中昵称是否不为空，然后更新
        if (profileUpdateDTO.getNickname() != null && !profileUpdateDTO.getNickname().isBlank()) {
            user.setNickname(profileUpdateDTO.getNickname());
        }
        return userRepository.save(user);
    }
}
