package com.ecommerce.service.impl;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	// Reusable mapper — converts entity to response DTO
	private ProductResponse mapToResponse(Product product) {
		ProductResponse response = new ProductResponse();
		response.setProductId(product.getProductId());
		response.setName(product.getName());
		response.setDescription(product.getDescription());
		response.setPrice(product.getPrice());
		response.setStock(product.getStock());
		response.setCategory(product.getCategory());
		response.setBrand(product.getBrand());
		response.setImageUrl(product.getImageUrl());
		response.setActive(product.getActive());
		response.setCreatedAt(product.getCreatedAt());
		response.setUpdatedAt(product.getUpdatedAt());
		return response;
	}

	@Override
	public ProductResponse addProduct(ProductRequest request) {
		Product product = new Product();
		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setStock(request.getStock());
		product.setCategory(request.getCategory());
		product.setBrand(request.getBrand());
		product.setImageUrl(request.getImageUrl());
		product.setActive(true);

		Product saved = productRepository.save(product);
		return mapToResponse(saved);
	}

	@Override
	public ProductResponse updateProduct(Long id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

		if (!product.getActive()) {
			throw new RuntimeException("Product not available");
		}

		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setStock(request.getStock());
		product.setCategory(request.getCategory());
		product.setBrand(request.getBrand());
		product.setImageUrl(request.getImageUrl());

		Product saved = productRepository.save(product);
		return mapToResponse(saved);
	}

	@Override
	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

		product.setActive(false);
		productRepository.save(product);
	}

	@Override
	public List<ProductResponse> getAllProducts() {
		return productRepository.findByActiveTrue().stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	@Override
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

		if (!product.getActive()) {
			throw new RuntimeException("Product not available");
		}

		return mapToResponse(product);
	}

	@Override
	public List<ProductResponse> searchProducts(String keyword, String category) {

		// Both keyword and category provided
		if (keyword != null && !keyword.isBlank() && category != null && !category.isBlank()) {
			return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword).stream()
					.filter(p -> p.getCategory().equalsIgnoreCase(category)).map(this::mapToResponse)
					.collect(Collectors.toList());
		}

		// Only keyword
		if (keyword != null && !keyword.isBlank()) {
			return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword).stream()
					.map(this::mapToResponse).collect(Collectors.toList());
		}

		// Only category
		if (category != null && !category.isBlank()) {
			return productRepository.findByCategoryAndActiveTrue(category).stream().map(this::mapToResponse)
					.collect(Collectors.toList());
		}

		// Nothing provided — return all
		return getAllProducts();
	}
}
