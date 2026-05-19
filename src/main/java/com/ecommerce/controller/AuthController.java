package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok("Password reset successfully");
	}

	// Protected endpoint — JwtFilter sets authentication before this runs
	// @AuthenticationPrincipal gives us the currently logged-in user
	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
		User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		return ResponseEntity.ok(new AuthResponse(
				null,
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getPhone(),
				user.getProfilePictureUrl(),
				user.getAddress()));
	}

	@PutMapping("/profile")
	public ResponseEntity<AuthResponse> updateProfile(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(authService.updateProfile(userDetails.getUsername(), request));
	}
}
