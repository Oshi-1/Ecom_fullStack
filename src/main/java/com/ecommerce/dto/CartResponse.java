package com.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartResponse {
	private List<CartItemResponse> items = new ArrayList<>();
	private Integer totalItems;
	private BigDecimal totalAmount;
}
