package com.serviciotecnico.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ProxyController {

    private final RestTemplate restTemplate;
    private final Map<String, String> routes = new LinkedHashMap<>();

    public ProxyController(RestTemplate restTemplate,
            @Value("${customer.service.url}") String customerUrl,
            @Value("${computer.service.url}") String computerUrl,
            @Value("${servicetype.service.url}") String serviceTypeUrl,
            @Value("${maintenance.service.url}") String maintenanceUrl) {
        this.restTemplate = restTemplate;
        routes.put("/api/auth", customerUrl);
        routes.put("/api/customers", customerUrl);
        routes.put("/api/computers", computerUrl);
        routes.put("/api/service-types", serviceTypeUrl);
        routes.put("/api/maintenance-tickets", maintenanceUrl);
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String targetBaseUrl = null;
        for (Map.Entry<String, String> route : routes.entrySet()) {
            if (path.equals(route.getKey()) || path.startsWith(route.getKey() + "/")) {
                targetBaseUrl = route.getValue();
                break;
            }
        }
        if (targetBaseUrl == null) {
            return ResponseEntity.notFound().build();
        }

        String queryString = request.getQueryString();
        String targetUrl = targetBaseUrl + path + (queryString != null ? "?" + queryString : "");

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.equalsIgnoreCase("host") || name.equalsIgnoreCase("content-length")) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.add(name, values.nextElement());
            }
        }

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    targetUrl, HttpMethod.valueOf(request.getMethod()), new HttpEntity<>(body, headers), byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((name, values) -> {
                if (!name.equalsIgnoreCase("transfer-encoding") && !name.equalsIgnoreCase("connection")) {
                    responseHeaders.put(name, values);
                }
            });
            return ResponseEntity.status(response.getStatusCode()).headers(responseHeaders).body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
