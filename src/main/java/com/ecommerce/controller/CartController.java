package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

	@Autowired
	private CartService cartService;

	@GetMapping
	public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
		return ResponseEntity.ok(cartService.getCart(userDetails));
	}

	@PostMapping("/items")
	public ResponseEntity<CartResponse> addToCart(@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody AddToCartRequest request) {
		return ResponseEntity.ok(cartService.addToCart(userDetails, request));
	}

	@PutMapping("/items/{cartItemId}")
	public ResponseEntity<CartResponse> updateCartItem(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long cartItemId, @Valid @RequestBody UpdateCartItemRequest request) {
		return ResponseEntity.ok(cartService.updateCartItem(userDetails, cartItemId, request));
	}

	@PutMapping("/items/product/{productId}")
	public ResponseEntity<CartResponse> updateCartItemByProduct(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long productId, @Valid @RequestBody UpdateCartItemRequest request) {
		return ResponseEntity.ok(cartService.updateCartItemByProduct(userDetails, productId, request));
	}

	@DeleteMapping("/items/{cartItemId}")
	public ResponseEntity<CartResponse> removeCartItem(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long cartItemId) {
		return ResponseEntity.ok(cartService.removeCartItem(userDetails, cartItemId));
	}

	@DeleteMapping
	public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
		cartService.clearCart(userDetails);
		return ResponseEntity.noContent().build();
	}
}
