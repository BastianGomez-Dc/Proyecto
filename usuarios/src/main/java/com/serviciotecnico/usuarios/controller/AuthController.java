package com.serviciotecnico.usuarios.controller;

import com.serviciotecnico.usuarios.dto.LoginRequest;
import com.serviciotecnico.usuarios.dto.LoginResponse;
import com.serviciotecnico.usuarios.security.JwtUtil;
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

    private static final String USUARIO = "admin";
    private static final String CONTRASENA = "admin123";

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (!USUARIO.equals(request.username()) || !CONTRASENA.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
        return new LoginResponse(jwtUtil.generarToken(request.username()));
    }
}
