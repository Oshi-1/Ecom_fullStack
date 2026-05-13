package com.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
	private Long cartItemId;
	private Long productId;
	private String name;
	private String category;
	private String imageUrl;
	private BigDecimal price;
	private Integer quantity;
	private Integer stock;
	private BigDecimal subtotal;
}
