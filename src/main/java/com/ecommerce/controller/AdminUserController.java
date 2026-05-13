package com.ecommerce.controller;

import com.ecommerce.dto.UserResponse;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		List<UserResponse> users = userRepository.findAll().stream()
				.map(user -> new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getRole()))
				.toList();

		return ResponseEntity.ok(users);
	}
}
