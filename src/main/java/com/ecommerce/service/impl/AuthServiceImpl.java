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
		user.setRole("USER");

		userRepository.save(user);

		String token = jwtUtil.generateToken(user.getEmail());
		return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole());
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		String email = request.getEmail().trim();

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		String token = jwtUtil.generateToken(user.getEmail());
		return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole());
	}

	@Override
	public void resetPassword(ForgotPasswordRequest request) {
		User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
				.orElseThrow(() -> new RuntimeException("No account found with this email"));

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}

}
