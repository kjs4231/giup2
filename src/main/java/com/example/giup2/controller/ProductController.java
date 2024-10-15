package com.example.giup2.controller;

import com.example.giup2.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {


    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products/{productId}/notifications/re-stock")
    public ResponseEntity<Void> restock(@PathVariable Long productId) {
        productService.restock(productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/products/{productId}/notifications/re-stock")
    public ResponseEntity<Void> retry(@PathVariable Long productId) {
        productService.retry(productId);
        return ResponseEntity.ok().build();
    }
}
