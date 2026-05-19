package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
	private Long userId;
	private String name;
	private String email;
	private String role;
	private String phone;
	private String profilePictureUrl;
	private String address;
}
