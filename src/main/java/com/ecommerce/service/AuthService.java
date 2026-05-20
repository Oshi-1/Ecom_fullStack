package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	AuthResponse getProfile(String email);

	void resetPassword(ForgotPasswordRequest request);

	AuthResponse updateProfile(String email, UpdateProfileRequest request);

	AuthResponse updateProfileImage(String email, MultipartFile image);

	User getUserById(Long userId);
}
