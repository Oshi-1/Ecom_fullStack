package com.ecommerce.service.impl;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Lazy // ← this one line fixes the circular dependency
	@Autowired
	private AuthenticationManager authenticationManager;

	private static final long MAX_PROFILE_IMAGE_SIZE = 2 * 1024 * 1024;
	private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	@Override
	public AuthResponse register(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase();

		if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new RuntimeException("Email already in use");
		}

		User user = new User();
		user.setName(request.getName());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setPasswordUpdatedAt(Instant.now());
		user.setPasswordChangeCount(0);
		user.setRole("USER");

		userRepository.save(user);

		String token = jwtUtil.generateToken(user.getEmail());
		return buildAuthResponse(token, user);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		String email = request.getEmail().trim();

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		String token = jwtUtil.generateToken(user.getEmail());
		return buildAuthResponse(token, user);
	}

	@Override
	public AuthResponse getProfile(String email) {
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return buildAuthResponse(null, user);
	}

	@Override
	public void resetPassword(ForgotPasswordRequest request) {
		User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
				.orElseThrow(() -> new RuntimeException("No account found with this email"));

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		user.setPasswordUpdatedAt(Instant.now());
		incrementPasswordChangeCount(user);
		userRepository.save(user);
	}

	@Override
	public AuthResponse updateProfile(String email, UpdateProfileRequest request) {
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		String updatedName = normalize(request.getName());
		String updatedEmail = normalize(request.getEmail());

		if (updatedName == null) {
			throw new RuntimeException("Name is required");
		}
		if (updatedEmail == null) {
			throw new RuntimeException("Email is required");
		}

		updatedEmail = updatedEmail.toLowerCase();
		userRepository.findByEmailIgnoreCase(updatedEmail)
				.filter(existing -> !existing.getUserId().equals(user.getUserId()))
				.ifPresent(existing -> {
					throw new RuntimeException("Email already in use");
				});

		String newPassword = normalize(request.getNewPassword());
		if (newPassword != null) {
			String currentPassword = normalize(request.getCurrentPassword());
			if (currentPassword == null) {
				throw new RuntimeException("Current password is required to set a new password");
			}
			if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
				throw new RuntimeException("Current password is incorrect");
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setPasswordUpdatedAt(Instant.now());
			incrementPasswordChangeCount(user);
		}

		user.setName(updatedName);
		user.setEmail(updatedEmail);
		user.setPhone(normalize(request.getPhone()));
		user.setAlternatePhone(normalize(request.getAlternatePhone()));
		user.setProfilePictureUrl(normalize(request.getProfilePictureUrl()));
		user.setAddress(normalize(request.getAddress()));

		User savedUser = userRepository.save(user);
		String token = email.equalsIgnoreCase(savedUser.getEmail()) ? null : jwtUtil.generateToken(savedUser.getEmail());
		return buildAuthResponse(token, savedUser);
	}

	@Override
	public AuthResponse updateProfileImage(String email, MultipartFile image) {
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (image == null || image.isEmpty()) {
			throw new RuntimeException("Profile image is required");
		}
		if (image.getSize() > MAX_PROFILE_IMAGE_SIZE) {
			throw new RuntimeException("Profile image cannot exceed 2 MB");
		}
		if (!ALLOWED_PROFILE_IMAGE_TYPES.contains(image.getContentType())) {
			throw new RuntimeException("Only JPG, PNG, and WebP images are allowed");
		}

		try {
			user.setProfileImage(image.getBytes());
		} catch (IOException ex) {
			throw new RuntimeException("Profile image could not be uploaded");
		}

		user.setProfileImageContentType(image.getContentType());
		user.setProfileImageFileName(image.getOriginalFilename());
		user.setProfileImageUpdatedAt(Instant.now());
		user.setProfilePictureUrl(null);

		return buildAuthResponse(null, userRepository.save(user));
	}

	@Override
	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private AuthResponse buildAuthResponse(String token, User user) {
		return new AuthResponse(
				user.getUserId(),
				token,
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getPhone(),
				user.getAlternatePhone(),
				resolveProfilePictureUrl(user),
				user.getAddress(),
				user.getPasswordUpdatedAt());
	}

	private String resolveProfilePictureUrl(User user) {
		if (user.getProfileImage() != null && user.getProfileImage().length > 0) {
			String version = user.getProfileImageUpdatedAt() == null
					? String.valueOf(System.currentTimeMillis())
					: String.valueOf(user.getProfileImageUpdatedAt().toEpochMilli());
			return "http://localhost:8081/api/auth/profile/image/" + user.getUserId() + "?v=" + version;
		}
		return user.getProfilePictureUrl();
	}

	private String normalize(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	private void incrementPasswordChangeCount(User user) {
		Integer currentCount = user.getPasswordChangeCount();
		user.setPasswordChangeCount(currentCount == null ? 1 : currentCount + 1);
	}
}
