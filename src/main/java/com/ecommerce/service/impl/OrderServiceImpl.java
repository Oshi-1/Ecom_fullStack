package com.ecommerce.service.impl;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public OrderResponse placeOrder(UserDetails userDetails, CheckoutRequest request) {
		User user = getUser(userDetails);
		List<CartItem> cartItems = cartItemRepository.findByUser_UserId(user.getUserId());

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Cart is empty");
		}

		Order order = new Order();
		order.setUser(user);
		order.setStatus("PLACED");
		order.setShippingAddress(request.getShippingAddress().trim());
		order.setPaymentMethod(request.getPaymentMethod());
		order.setContactNumber(normalizeContactNumber(request.getContactNumber()));

		BigDecimal totalAmount = BigDecimal.ZERO;
		int totalItems = 0;

		for (CartItem cartItem : cartItems) {
			Product product = cartItem.getProduct();
			validateProductForOrder(product, cartItem.getQuantity());

			BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProductId(product.getProductId());
			orderItem.setProductName(product.getName());
			orderItem.setCategory(product.getCategory());
			orderItem.setPrice(product.getPrice());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setSubtotal(subtotal);
			order.getItems().add(orderItem);

			product.setStock(product.getStock() - cartItem.getQuantity());
			productRepository.save(product);

			totalAmount = totalAmount.add(subtotal);
			totalItems += cartItem.getQuantity();
		}

		order.setTotalAmount(totalAmount);
		order.setTotalItems(totalItems);

		Order savedOrder = orderRepository.save(order);
		cartItemRepository.deleteAll(cartItems);

		return mapToResponse(savedOrder);
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponse> getMyOrders(UserDetails userDetails) {
		User user = getUser(userDetails);
		return orderRepository.findMyOrdersWithItems(user.getUserId()).stream()
				.map(this::mapToResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponse> getAllOrders() {
		return orderRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(this::mapToResponse)
				.toList();
	}

	private User getUser(UserDetails userDetails) {
		return userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private void validateProductForOrder(Product product, Integer quantity) {
		if (!product.getActive()) {
			throw new RuntimeException(product.getName() + " is not available");
		}

		if (quantity > product.getStock()) {
			throw new RuntimeException("Only " + product.getStock() + " items available for " + product.getName());
		}
	}

	private OrderResponse mapToResponse(Order order) {
		OrderResponse response = new OrderResponse();
		response.setOrderId(order.getOrderId());
		response.setUserId(order.getUser().getUserId());
		response.setCustomerName(order.getUser().getName());
		response.setCustomerEmail(order.getUser().getEmail());
		response.setTotalAmount(order.getTotalAmount());
		response.setTotalItems(order.getTotalItems());
		response.setStatus(order.getStatus());
		response.setShippingAddress(order.getShippingAddress());
		response.setPaymentMethod(order.getPaymentMethod());
		response.setContactNumber(order.getContactNumber());
		response.setCreatedAt(order.getCreatedAt());
		response.setItems(order.getItems().stream().map(this::mapItemToResponse).toList());
		return response;
	}

	private String normalizeContactNumber(String contactNumber) {
		if (contactNumber == null || contactNumber.isBlank()) {
			return null;
		}

		return contactNumber.trim();
	}

	private OrderItemResponse mapItemToResponse(OrderItem orderItem) {
		OrderItemResponse response = new OrderItemResponse();
		response.setProductId(orderItem.getProductId());
		response.setProductName(orderItem.getProductName());
		response.setCategory(orderItem.getCategory());
		response.setPrice(orderItem.getPrice());
		response.setQuantity(orderItem.getQuantity());
		response.setSubtotal(orderItem.getSubtotal());
		return response;
	}
}
