package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/areas")
    public ResponseEntity<ApiResponse> getDistinctAreaNames() { // 
        return ResponseEntity.ok(ApiResponse.success(locationService.getDistinctAreaNames()));
    }
}