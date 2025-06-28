package com.hellogreenfriends.backend.service;

import com.hellogreenfriends.backend.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * LocationService 类，用于处理地点相关的业务逻辑
 */
@Service
public class LocationService {
    private final LocationRepository locationRepository;
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * 获取所有不重复的校区区域名称，用于前端筛选器
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctAreaNames() {
        return locationRepository.findDistinctAreaNames();
    }
}