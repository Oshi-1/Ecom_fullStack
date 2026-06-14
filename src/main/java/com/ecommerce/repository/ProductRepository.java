package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// Get all visible products (active = true)
	List<Product> findByActiveTrue();

	// Get all products in a category
	List<Product> findByCategoryAndActiveTrue(String category);

	// Search by name containing a keyword (case-insensitive)
	List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

	boolean existsByNameIgnoreCaseAndActiveTrue(String name);

	Optional<Product> findByNameIgnoreCaseAndActiveTrue(String name);
}
