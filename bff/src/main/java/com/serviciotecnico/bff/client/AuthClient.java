package com.serviciotecnico.bff.client;

import com.serviciotecnico.bff.dto.AuthResponse;
import com.serviciotecnico.bff.dto.LoginRequest;
import com.serviciotecnico.bff.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {

    private final RestClient restClient;
    private final String baseUrl;

    public AuthClient(RestClient restClient, @Value("${usuarios.service.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public AuthResponse login(LoginRequest request) {
        return restClient.post()
                .uri(baseUrl + "/login")
                .body(request)
                .retrieve()
                .body(AuthResponse.class);
    }

    public AuthResponse register(RegisterRequest request) {
        return restClient.post()
                .uri(baseUrl + "/register")
                .body(request)
                .retrieve()
                .body(AuthResponse.class);
    }
}
