package com.ecommerce.security;

import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	// Spring Security calls this automatically during login
	// We load the user from DB so Spring can verify the password
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		com.ecommerce.entity.User appUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

		return org.springframework.security.core.userdetails.User.withUsername(appUser.getEmail())
				.password(appUser.getPassword()) // BCrypt hashed password
				.roles(appUser.getRole()) // "USER" or "ADMIN"
				.build();
	}
}