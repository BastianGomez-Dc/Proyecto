package com.serviciotecnico.customer.controller;

import com.serviciotecnico.customer.dto.LoginRequest;
import com.serviciotecnico.customer.dto.LoginResponse;
import com.serviciotecnico.customer.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (!USERNAME.equals(request.username()) || !PASSWORD.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(jwtUtil.generateToken(request.username()));
    }
}
