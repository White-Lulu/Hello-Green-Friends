package com.hellogreenfriends.backend.service;

import com.hellogreenfriends.backend.entity.Image;
import com.hellogreenfriends.backend.entity.Plant;
import com.hellogreenfriends.backend.entity.User;
import com.hellogreenfriends.backend.exception.ResourceNotFoundException;
import com.hellogreenfriends.backend.repository.ImageRepository;
import com.hellogreenfriends.backend.repository.PlantRepository;
import com.hellogreenfriends.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/*
 * ImageService 类用于处理与植物图片相关的业务逻辑。
 */
@Service
public class ImageService {

    private final Path fileStorageLocation;
    private final ImageRepository imageRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    public ImageService(@Value("${file.upload-dir}") String uploadDir,
                        ImageRepository imageRepository,
                        PlantRepository plantRepository,
                        UserRepository userRepository) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.imageRepository = imageRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;

        // 确保文件存储目录存在
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * 存储用户上传的植物图片，并与 Plant 实体关联。
     * @param file 用户上传的文件
     * @param plantId 关联的植物 ID
     * @param uploader 上传者的 User 实体
     * @return 保存后的 Image 实体
     */
    @Transactional
    public Image storeFile(MultipartFile file, Integer plantId, User uploader) {

        // 获取植物实体，如果不存在则抛出异常
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + plantId));

        // 调用通用保存方法，指定子目录为 "plants"
        String newFileName = this.saveFile(file, "plants");

        // 生成文件的可访问URL
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/plants/")
                .path(newFileName)
                .toUriString();

        // 创建 Image 实体并设置相关属性
        Image image = new Image();
        image.setPlant(plant); // 关联植物
        image.setUploader(uploader); // 设置上传者
        image.setImageUrl(fileDownloadUri); // 设置图片 URL
        image.setStatus(Image.ImageStatus.PENDING_REVIEW); // 默认设置为待审核状态

        // 保存 Image 实体
        return imageRepository.save(image);
    }

    /**
     * 为已发布的植物添加额外的图片。
     * @param plantId 植物 ID
     * @param uploader 上传者
     * @param file 图片文件
     * @return 保存后的 Image 实体
     */
    @Transactional
    public Image addPlantImage(Integer plantId, User uploader, MultipartFile file) {
        // 确保植物存在且已发布
        Plant plant = plantRepository.findByIdAndStatus(plantId, Plant.PlantStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published plant not found with id: " + plantId));

        // 调用通用文件保存方法
        String newFileName = this.saveFile(file, "plants");

        // 构建可访问的 URL
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/plants/")
                .path(newFileName)
                .toUriString();

        // 创建 Image 实体并设置属性
        Image image = new Image();
        image.setPlant(plant);
        image.setUploader(uploader);
        image.setImageUrl(fileDownloadUri);
        image.setStatus(Image.ImageStatus.PENDING_REVIEW); // 新上传的图片默认状态为待审核

        return imageRepository.save(image);
    }

    /**
     * 获取指定植物 ID 的所有图片
     * @param plantId 植物 ID
     * @return 图片列表
     */
    @Transactional(readOnly = true)
    public List<Image> getImagesByPlantId(Integer plantId) {
        return imageRepository.findByPlantId(plantId);
    }

    /**
     * 删除指定ID的图片。只有图片的上传者或管理员才能删除。
     * @param imageId 要删除的图片ID
     * @param currentUserId 执行删除操作的用户ID
     */
    @Transactional
    public void deleteImage(Integer imageId, Integer currentUserId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        // 只有图片的上传者或者管理员才能删除图片
        if (!image.getUploader().getId().equals(currentUserId) && currentUser.getRole() != User.UserRole.ADMIN) {
            throw new SecurityException("User is not authorized to delete this image.");
        }

        // 删除磁盘上的文件
        String fileName = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
        Path filePath = Paths.get(this.fileStorageLocation.toString(), "plants", fileName); // 植物图片都在plants子目录
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // 记录日志，但不阻止数据库删除，也许文件可能已经不存在或有其他问题
            System.err.println("Could not delete file from disk: " + filePath.toString() + " " + ex.getMessage());
        }
        // 删除数据库中的记录
        imageRepository.delete(image);
    }

    /**
     * 通用的文件保存方法。
     * @param file 用户上传的文件
     * @param subDirectory 子目录，例如 "avatars" 或 "plants"
     * @return 存储在磁盘上的唯一文件名
     */
    public String saveFile(MultipartFile file, String subDirectory) {
        // 确保子目录存在
        Path targetDirectory = this.fileStorageLocation.resolve(subDirectory);
        try {
            Files.createDirectories(targetDirectory);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory: " + subDirectory, ex);
        }

        // 生成唯一文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            throw new RuntimeException("Invalid file name: " + originalFileName, e);
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        try {
            Path targetLocation = targetDirectory.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + newFileName + ". Please try again!", ex);
        }
    }

    /**
     * 上传单个文件并返回其可访问的 URL。
     * 此方法不创建 Image 数据库记录，只处理文件本身。
     * @param file 用户上传的文件
     * @param subDirectory 存储的子目录，例如 "plants"
     * @return 文件的完整可访问 URL
     */
    public String storeAndGetUrl(MultipartFile file, String subDirectory) {
        // 调用前面的通用保存方法将文件保存到磁盘，并获取唯一文件名
        String newFileName = this.saveFile(file, subDirectory);

        // 构建并返回该文件的可访问 URL
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(subDirectory)
                .path("/")
                .path(newFileName)
                .toUriString();
    }


}