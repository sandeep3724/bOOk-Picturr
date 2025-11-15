package com.mvc.finall.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mvc.finall.model.Product;
import com.mvc.finall.model.ProductDetails;
import com.mvc.finall.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // NOTE: For image serving, you also need to expose the 'uploads' directory as a resource handler
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads");

    // ==============================================
    // Save/Update Logic
    // ==============================================

    public Product saveProduct(Product product, MultipartFile file) throws IOException {
        Product managedProduct;

        if (product.getId() == null) {
            // New Product logic
            product.setCreatedBy(System.getProperty("user.name"));
            product.setCreatedAt(LocalDateTime.now());

            managedProduct = new Product();
            managedProduct.setName(product.getName());
            managedProduct.setBrand(product.getBrand());
            managedProduct.setPrice(product.getPrice());
            managedProduct.setQuantity(product.getQuantity());
            managedProduct.setCreatedBy(product.getCreatedBy());
            managedProduct.setCreatedAt(product.getCreatedAt());
        } else {
            // Update existing product logic
            managedProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + product.getId()));
            
            // Update fields from the incoming product object
            managedProduct.setName(product.getName());
            managedProduct.setBrand(product.getBrand());
            managedProduct.setPrice(product.getPrice());
            managedProduct.setQuantity(product.getQuantity());
            
            // Preserve creation fields if not updating
            product.setCreatedBy(managedProduct.getCreatedBy());
            product.setCreatedAt(managedProduct.getCreatedAt());
        }
        
        // Save to get the ID for ProductDetails and Image Upload
        managedProduct = productRepository.save(managedProduct); 

        // ================= ProductDetails Calculation =================
        ProductDetails details = managedProduct.getDetails();
        if (details == null) {
            details = new ProductDetails();
            details.setProduct(managedProduct);
        }

        // Apply discount percentage from the form (if available)
        double discountPercent = (product.getDetails() != null) 
                                ? product.getDetails().getDiscountPercentage() 
                                : 0.0;
        
        details.setDiscountPercentage(discountPercent);
        details.calculateAmounts(); // Assume this method updates discountPrice, tax, net, and total
        managedProduct.setDetails(details);

        // ================= Image Upload & Replacement =================
        if (file != null && !file.isEmpty()) {
            Files.createDirectories(UPLOAD_DIR);

            // Use product ID as filename to overwrite existing
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = managedProduct.getId() + "." + fileExtension;
            Path filePath = UPLOAD_DIR.resolve(fileName);

            // Overwrite file if exists (using standard Java NIO Files.copy or Files.write)
            Files.write(filePath, file.getBytes());

            // The image URL must be accessible via HTTP/web server (e.g., Spring resource handler)
            managedProduct.setImageUrl("/uploads/" + fileName);
        }

        return productRepository.save(managedProduct);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "png";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1 || dotIndex == fileName.length() - 1) 
               ? "png" 
               : fileName.substring(dotIndex + 1).toLowerCase();
    }

    // ==============================================
    // Retrieval and Deletion
    // ==============================================

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        // Sort by ID for consistent display order
        Collections.sort(products, (a, b) -> a.getId().compareTo(b.getId())); 
        return products;
    }

    public boolean deleteProductById(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ==============================================
    // NEW FEATURE: Search by Name
    // ==============================================
    
    /**
     * Finds products whose name contains the search term (case-insensitive).
     * This relies on a correctly defined method in ProductRepository:
     * List<Product> findByNameContainingIgnoreCase(String name);
     */
    public List<Product> findByName(String name) {
        // Assume the repository method findByNameContainingIgnoreCase exists
        return productRepository.findByNameContainingIgnoreCase(name); 
    }
}