package com.ecommerce.config;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountConfig {

	@Value("${app.admin.name:Admin}")
	private String adminName;

	@Value("${app.admin.email:admin@gmail.com}")
	private String adminEmail;

	@Value("${app.admin.password:admin123}")
	private String adminPassword;

	@Bean
	CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElseGet(User::new);
			admin.setName(adminName);
			admin.setEmail(adminEmail);
			admin.setPassword(passwordEncoder.encode(adminPassword));
			admin.setRole("ADMIN");
			userRepository.save(admin);

			userRepository.findAll().stream()
					.filter(user -> "ADMIN".equalsIgnoreCase(user.getRole()))
					.filter(user -> !adminEmail.equalsIgnoreCase(user.getEmail()))
					.forEach(user -> {
						user.setRole("USER");
						userRepository.save(user);
					});
		};
	}
}
