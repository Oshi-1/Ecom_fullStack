package com.ecommerce.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {

	private String name;

	private String email;

	private String phone;

	private String alternatePhone;

	private String profilePictureUrl;

	private String address;

	private String currentPassword;

	@Pattern(regexp = "^$|^.{6,100}$", message = "Password must be at least 6 characters")
	private String newPassword;
}
