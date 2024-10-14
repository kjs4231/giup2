package com.example.giup2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int restockCount;
    private int stock;

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setRestockCount(int restockCount) {
        this.restockCount = restockCount;
    }
}
