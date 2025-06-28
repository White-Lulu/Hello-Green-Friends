package com.hellogreenfriends.backend.config;

import com.hellogreenfriends.backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@Profile("dev")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 在 http 配置链的开始部分启用 CORS
                .cors(withDefaults())

                // 禁用 CSRF，因为使用 JWT，是无状态的
                .csrf(AbstractHttpConfigurer::disable)

                // 允许 H2 控制台在 iframe 中显示
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",      // 登录和注册
                                "/api/plants/**",      // 公开的植物信息
                                "/api/locations/**",   // 公开的地点信息
                                "/api/tags/**",        // 公开的标签信息
                                "/swagger-ui/**",      // Swagger UI
                                "/v3/api-docs/**",     // OpenAPI specs
                                "/h2-console/**"       // 允许访问 H2 控制台
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // 允许已认证用户上传额外图片和删除图片
                        .requestMatchers(HttpMethod.POST, "/api/plants/{plantId}/additional-images").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/images/{imageId}").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 管理员接口
                        .anyRequest().authenticated() // 其他所有请求都需要认证
                )

                // 设置 Session 管理策略为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                
        // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 定义详细的 CORS 规则
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许来自前端开发服务器的跨域请求
        configuration.setAllowedOrigins(List.of("http://localhost:8081"));
        // 允许所有常见的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有请求头
        configuration.setAllowedHeaders(List.of("*"));
        // 允许凭证（如 Cookies），虽然我们用的是 JWT，但最好加上
        configuration.setAllowCredentials(true);
        // 对所有路径应用此CORS 配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}