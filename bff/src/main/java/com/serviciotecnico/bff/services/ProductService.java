package com.serviciotecnico.bff.services;

import com.serviciotecnico.bff.dto.ProductRequest;
import com.serviciotecnico.bff.dto.ProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final List<ProductResponse> products = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<ProductResponse> findAll() {
        return products;
    }

    public ProductResponse findById(long id) {
        return products.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
    }

    public ProductResponse create(ProductRequest request) {
        ProductResponse product = new ProductResponse(sequence.incrementAndGet(), request.name(), request.price());
        products.add(product);
        return product;
    }

    public ProductResponse update(long id, ProductRequest request) {
        ProductResponse existing = findById(id);
        ProductResponse updated = new ProductResponse(existing.id(), request.name(), request.price());
        products.set(products.indexOf(existing), updated);
        return updated;
    }

    public void delete(long id) {
        ProductResponse existing = findById(id);
        products.remove(existing);
    }
}
