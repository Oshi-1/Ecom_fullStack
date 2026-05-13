package com.ecommerce.controller;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

	@Autowired
	private ProductService productService;

	// POST /api/products - add a product.
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.ok(productService.addProduct(request));
	}

	// PUT /api/products/{id} - update a product.
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductResponse> updateProduct(
			@PathVariable Long id,
			@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	// DELETE /api/products/{id} - soft delete a product.
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	// GET /api/products - get all active products.
	@GetMapping
	public ResponseEntity<List<ProductResponse>> getAllProducts() {
		return ResponseEntity.ok(productService.getAllProducts());
	}

	// GET /api/products/{id} - get one product by ID.
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

	// GET /api/products/search?keyword=phone&category=Electronics
	@GetMapping("/search")
	public ResponseEntity<List<ProductResponse>> search(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String category) {
		return ResponseEntity.ok(productService.searchProducts(keyword, category));
	}
}
