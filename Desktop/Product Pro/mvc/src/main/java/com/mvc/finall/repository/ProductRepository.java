package com.mvc.finall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mvc.finall.model.Product;
import java.util.List; // Import List for the new method

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

    /**
     * Finds products where the 'name' field contains the specified string, 
     * performing a case-insensitive search (equivalent to SQL's LIKE '%search_term%').
     * Spring Data JPA automatically implements this method based on its name.
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}