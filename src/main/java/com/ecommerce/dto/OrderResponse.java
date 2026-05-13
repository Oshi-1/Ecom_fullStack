package com.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderResponse {
	private Long orderId;
	private Long userId;
	private String customerName;
	private String customerEmail;
	private BigDecimal totalAmount;
	private Integer totalItems;
	private String status;
	private String shippingAddress;
	private String paymentMethod;
	private String contactNumber;
	private LocalDateTime createdAt;
	private List<OrderItemResponse> items = new ArrayList<>();
}
