package com.ecommerce.service.impl;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public CartResponse getCart(UserDetails userDetails) {
		User user = getUser(userDetails);
		return buildCartResponse(cartItemRepository.findByUser_UserId(user.getUserId()));
	}

	@Override
	@Transactional
	public CartResponse addToCart(UserDetails userDetails, AddToCartRequest request) {
		User user = getUser(userDetails);
		Product product = getActiveProduct(request.getProductId());
		Integer quantityToAdd = request.getQuantity() == null ? 1 : request.getQuantity();

		CartItem cartItem = cartItemRepository
				.findByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId())
				.orElseGet(() -> createCartItem(user, product));

		int newQuantity = cartItem.getQuantity() + quantityToAdd;
		validateStock(product, newQuantity);
		cartItem.setQuantity(newQuantity);
		cartItemRepository.save(cartItem);

		return buildCartResponse(cartItemRepository.findByUser_UserId(user.getUserId()));
	}

	@Override
	@Transactional
	public CartResponse updateCartItem(UserDetails userDetails, Long cartItemId, UpdateCartItemRequest request) {
		User user = getUser(userDetails);
		CartItem cartItem = getCartItem(cartItemId, user.getUserId());
		return updateCartItemQuantity(user, cartItem, request.getQuantity());
	}

	@Override
	@Transactional
	public CartResponse updateCartItemByProduct(UserDetails userDetails, Long productId, UpdateCartItemRequest request) {
		User user = getUser(userDetails);
		CartItem cartItem = cartItemRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), productId)
				.orElseThrow(() -> new RuntimeException("Cart item not found"));
		return updateCartItemQuantity(user, cartItem, request.getQuantity());
	}

	@Override
	@Transactional
	public CartResponse removeCartItem(UserDetails userDetails, Long cartItemId) {
		User user = getUser(userDetails);
		CartItem cartItem = getCartItem(cartItemId, user.getUserId());
		cartItemRepository.delete(cartItem);

		return buildCartResponse(cartItemRepository.findByUser_UserId(user.getUserId()));
	}

	@Override
	@Transactional
	public void clearCart(UserDetails userDetails) {
		User user = getUser(userDetails);
		cartItemRepository.findByUser_UserId(user.getUserId()).forEach(cartItemRepository::delete);
	}

	private CartItem createCartItem(User user, Product product) {
		CartItem cartItem = new CartItem();
		cartItem.setUser(user);
		cartItem.setProduct(product);
		cartItem.setQuantity(0);
		return cartItem;
	}

	private User getUser(UserDetails userDetails) {
		return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private Product getActiveProduct(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		if (!product.getActive()) {
			throw new RuntimeException("Product is not available");
		}

		return product;
	}

	private CartItem getCartItem(Long cartItemId, Long userId) {
		return cartItemRepository.findByCartItemIdAndUser_UserId(cartItemId, userId)
				.orElseThrow(() -> new RuntimeException("Cart item not found"));
	}

	private CartResponse updateCartItemQuantity(User user, CartItem cartItem, Integer quantity) {
		validateStock(cartItem.getProduct(), quantity);

		cartItem.setQuantity(quantity);
		cartItemRepository.save(cartItem);

		return buildCartResponse(cartItemRepository.findByUser_UserId(user.getUserId()));
	}

	private void validateStock(Product product, Integer quantity) {
		if (quantity > product.getStock()) {
			throw new RuntimeException("Only " + product.getStock() + " items available in stock");
		}
	}

	private CartResponse buildCartResponse(List<CartItem> cartItems) {
		List<CartItemResponse> itemResponses = cartItems.stream().map(this::mapToResponse).toList();
		BigDecimal totalAmount = itemResponses.stream()
				.map(CartItemResponse::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		Integer totalItems = itemResponses.stream()
				.map(CartItemResponse::getQuantity)
				.reduce(0, Integer::sum);

		CartResponse response = new CartResponse();
		response.setItems(itemResponses);
		response.setTotalAmount(totalAmount);
		response.setTotalItems(totalItems);
		return response;
	}

	private CartItemResponse mapToResponse(CartItem cartItem) {
		Product product = cartItem.getProduct();
		BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

		CartItemResponse response = new CartItemResponse();
		response.setCartItemId(cartItem.getCartItemId());
		response.setProductId(product.getProductId());
		response.setName(product.getName());
		response.setCategory(product.getCategory());
		response.setImageUrl(product.getImageUrl());
		response.setPrice(product.getPrice());
		response.setQuantity(cartItem.getQuantity());
		response.setStock(product.getStock());
		response.setSubtotal(subtotal);
		return response;
	}
}
