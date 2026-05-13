package com.ecommerce.service;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.OrderResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface OrderService {
	OrderResponse placeOrder(UserDetails userDetails, CheckoutRequest request);

	List<OrderResponse> getMyOrders(UserDetails userDetails);

	List<OrderResponse> getAllOrders();
}
