package com.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
	private Long productId;
	private String productName;
	private String category;
	private BigDecimal price;
	private Integer quantity;
	private BigDecimal subtotal;
}
