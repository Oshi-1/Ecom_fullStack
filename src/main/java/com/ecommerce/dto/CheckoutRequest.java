package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {

	@NotBlank(message = "Shipping address is required")
	@Size(max = 500, message = "Shipping address must be 500 characters or less")
	private String shippingAddress;

	@NotBlank(message = "Payment method is required")
	@Pattern(regexp = "COD|CARD|UPI", message = "Payment method must be COD, CARD, or UPI")
	private String paymentMethod;

	@Size(max = 20, message = "Contact number must be 20 characters or less")
	private String contactNumber;
}
