package com.example.backend.controller;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.TokenResponse;
import com.example.backend.service.UserService;
import com.example.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    // @Autowired
    // public AuthController(@NonNull UserService userService, @NonNull JwtUtil
    // jwtUtil) {
    // this.userService = userService;
    // this.jwtUtil = jwtUtil;
    // }
    @GetMapping("/")
    public String home() {
        return "Welcome to the Spring Boot Application!";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String tokenResponse = userService.authenticateUser(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    //    @PostMapping("/ass")
//    public ResponseEntity<String> ass(@RequestBody LoginRequest loginRequest) {
//        String tokenResponse = userService.extractEmail(loginRequest);
//        return ResponseEntity.ok("Login successful! Token: " + tokenResponse);
//    }
    @PostMapping("/login2")
    public ResponseEntity<TokenResponse> login2(@RequestBody LoginRequest request) {
        // Dummy authentication - Replace with real user validation
        if ("admin".equals(request.getUsername()) && "admin".equals(request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(TokenResponse.builder().token(token).build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        // Get Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null ) {
            return ResponseEntity.status(401).body("Token missing or invalid");
        }

        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Extract username from token
        String username = jwtUtil.extractEmail(token);

        return ResponseEntity.ok("Authenticated user: " + username);
    }
}