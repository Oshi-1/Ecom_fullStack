package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

	@NotBlank(message = "Name is required")
	@Size(max = 80, message = "Name cannot exceed 80 characters")
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 120, message = "Email cannot exceed 120 characters")
	private String email;

	@Pattern(regexp = "^$|^[0-9+\\-()\\s]{7,20}$", message = "Phone number must be valid")
	private String phone;

	@Size(max = 500, message = "Profile picture URL cannot exceed 500 characters")
	@Pattern(regexp = "^$|^https?://.+", message = "Profile picture must be a valid URL")
	private String profilePictureUrl;

	@Size(max = 600, message = "Address cannot exceed 600 characters")
	private String address;

	private String currentPassword;

	@Pattern(regexp = "^$|^.{6,100}$", message = "Password must be at least 6 characters")
	private String newPassword;
}
