package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

	@Autowired
	private AuthService authService;

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
		return ResponseEntity.ok(authService.getProfile(userDetails.getUsername()));
	}

	@PutMapping("/profile")
	public ResponseEntity<AuthResponse> updateProfile(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(authService.updateProfile(userDetails.getUsername(), request));
	}

	@PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AuthResponse> updateProfileImage(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("image") MultipartFile image) {
		return ResponseEntity.ok(authService.updateProfileImage(userDetails.getUsername(), image));
	}

	@GetMapping("/profile/image/{userId}")
	public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
		User user = authService.getUserById(userId);

		if (user.getProfileImage() == null || user.getProfileImage().length == 0) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(user.getProfileImageContentType()))
				.body(user.getProfileImage());
	}
}
