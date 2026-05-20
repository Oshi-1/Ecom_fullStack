package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

@Data
@AllArgsConstructor
public class AuthResponse {
	private Long userId;
	private String token;
	private String name;
	private String email;
	private String role;
	private String phone;
	private String alternatePhone;
	private String profilePictureUrl;
	private String address;
	private Instant passwordUpdatedAt;
}
