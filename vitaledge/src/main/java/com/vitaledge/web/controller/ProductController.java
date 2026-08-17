package com.vitaledge.web.controller;

import com.vitaledge.service.ProductService;
import com.vitaledge.web.dto.product.ProductRequest;
import com.vitaledge.web.dto.product.ProductResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/v1/products")
    public ResponseEntity<List<ProductResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(name = "age_group", required = false) String ageGroup,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.listActive(category, ageGroup, q, page, size));
    }

    @GetMapping("/api/v1/products/{id}")
    public ResponseEntity<ProductResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getActiveDetail(id));
    }

    @PostMapping("/api/v1/admin/products")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/api/v1/admin/products/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/api/v1/admin/products/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted."));
    }
}