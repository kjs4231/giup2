package com.example.giup2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int stock;

    private int restockCount;

    public void setStock(int stock) {
        this.stock = stock;
    }
    public void setRestockCount(int restockCount) {
        this.restockCount = restockCount;
    }
}
