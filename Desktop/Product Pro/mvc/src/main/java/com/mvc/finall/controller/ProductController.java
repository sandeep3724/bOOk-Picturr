package com.mvc.finall.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mvc.finall.model.Product;
import com.mvc.finall.service.ProductService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    // ==============================================
    // Standard Navigation Mappings (Omitted for brevity, assumed correct)
    // ==============================================

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "home";
    }

    @GetMapping("/about")
    public String about(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "about";
    }

    @GetMapping("/contact")
    public String contact(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "contact";
    }
    
    @GetMapping("/support")
    public String support(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "support"; 
    }


    // ==============================================
    // Product List & Forms (Omitted for brevity, assumed correct)
    // ==============================================

    @GetMapping("/products")
    public String listProducts(HttpServletRequest request, Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("currentUri", request.getRequestURI());
        return "products/list";
    }

    @GetMapping("/products/add")
    public String showAddForm(HttpServletRequest request, Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("currentUri", request.getRequestURI());
        return "products/add";
    }

    @PostMapping("/products")
    public String addProduct(@Valid @ModelAttribute Product product,
                             BindingResult br,
                             @RequestParam MultipartFile file,
                             HttpServletRequest request, Model model) throws IOException {
        
        if (br.hasErrors()) {
             model.addAttribute("currentUri", request.getRequestURI());
             return "products/add";
        }
        productService.saveProduct(product, file);
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String showEdit(@PathVariable Long id, HttpServletRequest request, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/products";
        model.addAttribute("product", product);
        model.addAttribute("currentUri", "/products"); 
        return "products/edit";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                 @Valid @ModelAttribute Product product,
                                 BindingResult br,
                                 @RequestParam MultipartFile file,
                                 HttpServletRequest request, Model model) throws IOException {
        
        if (br.hasErrors()) {
            model.addAttribute("currentUri", "/products"); 
            return "products/edit";
        }
        product.setId(id);
        productService.saveProduct(product, file);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }

    // ==============================================
    // Search Mapping (FINAL FIX)
    // ==============================================
    
    @GetMapping("/search")
    public String searchProductById(
            @RequestParam(required = false) String query, 
            Model model) {
        
        model.addAttribute("currentUri", "/products"); 
        
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("error", "⚠️ Please enter a Product ID or Name to search.");
            model.addAttribute("products", productService.getAllProducts());
            return "products/list";
        }
        
        String trimmedQuery = query.trim();

        try {
            // 1. ATTEMPT SEARCH BY ID
            Long id = Long.parseLong(trimmedQuery);
            
            Product p = productService.getProductById(id);
            if (p != null) {
                model.addAttribute("products", List.of(p));
            } else {
                model.addAttribute("products", List.of());
                model.addAttribute("error", "⚠️ No product found with ID: " + id);
            }
            
        } catch (NumberFormatException e) {
            // 2. IF PARSING FAILS (It's text/name), FALLBACK TO SEARCH BY NAME
            
            // Call the new service method that uses findByNameContainingIgnoreCase
            List<Product> results = productService.findByName(trimmedQuery);
            
            if (!results.isEmpty()) {
                model.addAttribute("products", results);
            } else {
                model.addAttribute("products", List.of());
                model.addAttribute("error", "⚠️ No products found matching name: '" + trimmedQuery + "'");
            }
        }
        
        return "products/list";
    }
}