package com.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
	private Long productId;
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stock;
	private String category;
	private String imageUrl;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
