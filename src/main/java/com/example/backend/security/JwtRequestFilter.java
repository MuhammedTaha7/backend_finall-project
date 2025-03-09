package com.example.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        System.out.println("Request URI: " + requestURI);

        // ✅ Bypass authentication for specific endpoints
        if (requestURI.contains("/api/login") ||
                requestURI.contains("/api/register") ||
                requestURI.contains("/api/resetPassword")) {
            System.out.println("Bypassing JWT filter for: " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        String jwt = null;
        String email = null;

        // ✅ Try to extract token from Authorization header
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            email = jwtUtil.extractUsername(jwt);
        } else {
            // ✅ If token is not in header, try to extract from cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                        .filter(cookie -> "jwtToken".equals(cookie.getName()))
                        .findFirst();
                if (jwtCookie.isPresent()) {
                    jwt = jwtCookie.get().getValue();
                    email = jwtUtil.extractUsername(jwt);
                }
            }
        }

        // ✅ Validate the token and set security context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.isTokenValid(jwt, email)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("JWT validated successfully for: " + email);
            } else {
                System.out.println("Invalid or expired JWT token for: " + email);
            }
        }

        chain.doFilter(request, response);
    }
}
