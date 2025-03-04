package com.example.backend.config;

import com.example.backend.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // طباعة لتأكيد الوصول إلى إعدادات الأمان
        System.out.println("Security config accessed");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // تمكين CORS باستخدام المصدر المعدل
                .csrf(csrf -> csrf.disable()) // تعطيل CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // السماح بكل طلبات /api/*
                        .anyRequest().authenticated() // طلب المصادقة لأي طلبات أخرى
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://13.49.225.86:3000"); // أضف الـ IP العام
        configuration.addAllowedOrigin("http://localhost:3000"); // السماح بالأصل المطلوب
        configuration.addAllowedHeader("*"); // السماح بكل الرؤوس
        configuration.addAllowedMethod("*"); // السماح بكل الطرق
        configuration.setAllowCredentials(true); // السماح بإرسال ملفات تعريف الارتباط

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // تسجيل الإعدادات لكل المسارات
        return source;
    }
}
