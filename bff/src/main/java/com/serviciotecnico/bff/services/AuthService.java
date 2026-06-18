package com.serviciotecnico.bff.services;

import com.serviciotecnico.bff.client.AuthClient;
import com.serviciotecnico.bff.dto.AuthResponse;
import com.serviciotecnico.bff.dto.LoginRequest;
import com.serviciotecnico.bff.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthClient authClient;

    public AuthService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public AuthResponse login(LoginRequest request) {
        return authClient.login(request);
    }

    public AuthResponse register(RegisterRequest request) {
        return authClient.register(request);
    }
}
