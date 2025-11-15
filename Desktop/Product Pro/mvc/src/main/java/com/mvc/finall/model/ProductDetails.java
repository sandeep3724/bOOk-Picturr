package com.mvc.finall.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 🔹 Each ProductDetails is linked to one Product
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🔹 Default values for pricing fields
    private double discountPercentage = 0.0; 
    private double discountPrice = 10.0;      
    private double taxPercentage = 18.0;    
    private double taxAmount = 0.0;
    private double netAmount = 0.0;
    private double totalPrice = 0.0;

    // 🔹 Calculate amounts based on product price, discount, and tax
    public void calculateAmounts() {
        if (product != null) {
            double price = product.getPrice();
            discountPrice = price * (discountPercentage / 100);
            netAmount = price - discountPrice;
            taxAmount = netAmount * (taxPercentage / 100);
            totalPrice = netAmount + taxAmount;
        } else {
            discountPrice = 0.0;
            netAmount = 0.0;
            taxAmount = 0.0;
            totalPrice = 0.0;
        }
    }

}
