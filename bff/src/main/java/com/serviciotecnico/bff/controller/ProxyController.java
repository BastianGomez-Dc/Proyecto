package com.serviciotecnico.bff.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;

@RestController
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Value("${usuarios.service.url:http://localhost:8081}")
    private String usuariosUrl;

    @Value("${computador.service.url:http://localhost:8080}")
    private String computadorUrl;

    @Value("${tiposervicio.service.url:http://localhost:8082}")
    private String tiposervicioUrl;

    @Value("${mantencion.service.url:http://localhost:8084}")
    private String mantencionUrl;

    private Map<String, String> routes;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    private void initRoutes() {
        routes = Map.of(
            "/api/auth",          usuariosUrl,
            "/api/usuarios",      usuariosUrl,
            "/api/computadores",  computadorUrl,
            "/api/tiposervicios", tiposervicioUrl,
            "/api/mantenciones",  mantencionUrl
        );
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        String path = request.getRequestURI();
        String queryString = request.getQueryString();

        String targetBase = routes.entrySet().stream()
                .filter(e -> path.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada: " + path));

        String targetUrl = targetBase + path + (queryString != null ? "?" + queryString : "");
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(name -> headers.add(name, request.getHeader(name)));
        headers.remove(HttpHeaders.HOST);

        logger.info("BFF → {} {}", method, targetUrl);

        try {
            return restTemplate.exchange(targetUrl, method, new HttpEntity<>(body, headers), byte[].class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        }
    }
}
