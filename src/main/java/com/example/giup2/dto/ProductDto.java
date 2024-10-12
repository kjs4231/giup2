package com.example.giup2.dto;

public class ProductDto {

    private Long id;
    private String name;
    private int stock;
    private int restockCount;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getRestockCount() {
        return restockCount;
    }

    public void setRestockCount(int restockCount) {
        this.restockCount = restockCount;
    }
}
