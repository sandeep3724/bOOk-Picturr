package com.mvc.finall.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

   
    private String name;

   
    private String brand;

    
    private double price;

    
    private int quantity;

    private String imageUrl;
    private String createdBy;
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductDetails details;

    public void setDetails(ProductDetails details) {
        if (details != null) {
            details.setProduct(this);
        }
        this.details = details;
    }
}
