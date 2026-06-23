package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CheckoutRequest {

	@Valid
	@NotNull(message = "User details are required")
	private CheckoutUserDetails userDetails;

	@NotBlank(message = "Shipping address is required")
	@Size(max = 500, message = "Shipping address must be 500 characters or less")
	private String shippingAddress;

	@Valid
	@NotEmpty(message = "Cart items are required")
	private List<CheckoutCartItem> cartItems = new ArrayList<>();

	@NotNull(message = "Total amount is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Total amount must be zero or greater")
	private BigDecimal totalAmount;

	@NotBlank(message = "Payment method is required")
	@Pattern(regexp = "COD|CARD|UPI", message = "Payment method must be COD, CARD, or UPI")
	private String paymentMethod;

	@NotBlank(message = "Contact number is required")
	@Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "Contact number is invalid")
	@Size(max = 20, message = "Contact number must be 20 characters or less")
	private String contactNumber;

	@Data
	public static class CheckoutUserDetails {
		@NotBlank(message = "Customer name is required")
		@Size(max = 100, message = "Customer name must be 100 characters or less")
		private String name;

		@NotBlank(message = "Customer email is required")
		@Email(message = "Customer email is invalid")
		@Size(max = 150, message = "Customer email must be 150 characters or less")
		private String email;
	}

	@Data
	public static class CheckoutCartItem {
		@NotNull(message = "Product id is required")
		private Long productId;

		@NotBlank(message = "Product name is required")
		@Size(max = 255, message = "Product name must be 255 characters or less")
		private String name;

		@NotNull(message = "Quantity is required")
		@Positive(message = "Quantity must be greater than zero")
		private Integer quantity;

		@NotNull(message = "Subtotal is required")
		@DecimalMin(value = "0.0", inclusive = true, message = "Subtotal must be zero or greater")
		private BigDecimal subtotal;
	}
}
