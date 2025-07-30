package com.example.backend.common.security;

import com.example.backend.eduSphere.entity.UserEntity;
import com.example.backend.eduSphere.repository.UserRepository;
import com.example.backend.eduSphere.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        if (requestURI.contains("/api/login") ||
                requestURI.contains("/api/register") ||
                requestURI.contains("/api/resetPassword")) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = null;
        String email = null;

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            email = jwtUtil.extractEmail(jwt);
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        try {
                            email = jwtUtil.extractEmail(jwt);
                            break;
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extractUsername(jwt);
                if (jwtUtil.isTokenValid(jwt, username)) {
                    Optional<UserEntity> userOptional = userRepository.findByEmail(email);
                    if (userOptional.isPresent()) {
                        String userId = userOptional.get().getId();

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userId, null, null);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception ignored) {}
        }

        chain.doFilter(request, response);
    }
}
