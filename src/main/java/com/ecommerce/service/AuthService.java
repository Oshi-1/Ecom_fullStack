package com.ecommerce.service;

import com.ecommerce.dto.*;

public interface AuthService {
	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);
}