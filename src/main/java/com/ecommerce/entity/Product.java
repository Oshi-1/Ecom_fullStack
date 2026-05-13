package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;

	@Column(nullable = false)
	@NotBlank(message = "Product name is required")
	private String name;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
	private BigDecimal price;

	@Column(nullable = false)
	@NotNull(message = "Stock is required")
	@Min(value = 0, message = "Stock cannot be negative")
	private Integer stock;

	@Column(nullable = false)
	private String category;

	private String imageUrl;

	// true = visible in store, false = hidden (admin can deactivate)
	@Column(nullable = false)
	private Boolean active = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	// Runs automatically before first save
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	// Runs automatically before every update
	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}