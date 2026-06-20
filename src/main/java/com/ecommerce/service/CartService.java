package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface CartService {

	CartResponse getCart(UserDetails userDetails);

	CartResponse addToCart(UserDetails userDetails, AddToCartRequest request);

	CartResponse updateCartItem(UserDetails userDetails, Long cartItemId, UpdateCartItemRequest request);

	CartResponse updateCartItemByProduct(UserDetails userDetails, Long productId, UpdateCartItemRequest request);

	CartResponse removeCartItem(UserDetails userDetails, Long cartItemId);

	void clearCart(UserDetails userDetails);
}
