package com.example.backend.controller;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.service.UserService;
import com.example.backend.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        LoginResponse loginResponse = userService.authenticateUser(loginRequest);

        // ✅ Store JWT Token in HTTP-only Cookie
        Cookie jwtCookie = new Cookie("jwtToken", loginResponse.getToken());
        jwtCookie.setHttpOnly(true); // Prevent JavaScript access
        jwtCookie.setSecure(false);  // Allow use on localhost (should be true for production)
        jwtCookie.setPath("/");      // Available across the app
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // Expires in 7 days
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Login Successful");
    }

    @GetMapping("/auth/user")
    public ResponseEntity<AuthResponse> getUserData(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    String token = cookie.getValue();

                    // ✅ Extract username (email) and role from JWT
                    String username = jwtUtil.extractUsername(token);
                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRole(token);

                    return ResponseEntity.ok(new AuthResponse(username,email, role));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // ✅ Remove JWT cookie
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Allow localhost
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}
