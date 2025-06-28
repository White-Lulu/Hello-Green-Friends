package com.hellogreenfriends.backend.controller;

import com.hellogreenfriends.backend.dto.ApiResponse;
import com.hellogreenfriends.backend.dto.JwtAuthenticationResponseDTO;
import com.hellogreenfriends.backend.dto.LoginRequestDTO;

import com.hellogreenfriends.backend.dto.UserRegistrationDTO;
import com.hellogreenfriends.backend.security.JwtTokenProvider;
import com.hellogreenfriends.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        JwtAuthenticationResponseDTO jwtDto = new JwtAuthenticationResponseDTO(jwt);
        return ResponseEntity.ok(ApiResponse.success(jwtDto));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerUser(registrationDTO);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully!"));
    }
}