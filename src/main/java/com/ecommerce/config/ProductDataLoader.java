package com.ecommerce.config;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ProductDataLoader {

	@Bean
	CommandLineRunner seedProducts(ProductRepository productRepository) {
		return args -> {
			List<Product> demoProducts = List.of(
					createProduct("Wireless Headphones", "Comfortable Bluetooth headphones with deep bass and long battery life.",
							"Electronics", "2499.00", 18,
							"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80"),
					createProduct("Smart Watch", "Fitness tracking smartwatch with heart-rate monitor and notification support.",
							"Electronics", "3999.00", 12,
							"https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80"),
					createProduct("Running Shoes", "Lightweight everyday running shoes with breathable mesh.",
							"Footwear", "1899.00", 24,
							"https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80"),
					createProduct("Travel Backpack", "Durable backpack with laptop storage and organized compartments.",
							"Accessories", "1299.00", 30,
							"https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80"),
					createProduct("Cotton Casual Shirt", "Soft cotton shirt with a clean regular fit for daily wear.",
							"Fashion", "899.00", 40,
							"https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=900&q=80"),
					createProduct("Ceramic Coffee Mug", "Matte finish ceramic mug for coffee, tea, and desk setups.",
							"Home", "349.00", 55,
							"https://images.unsplash.com/photo-1517256064527-09c73fc73e38?auto=format&fit=crop&w=900&q=80"),
					createProduct("Desk Lamp", "Modern LED desk lamp with adjustable arm and warm light mode.",
							"Home", "1199.00", 22,
							"https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=80"),
					createProduct("Bluetooth Speaker", "Portable wireless speaker with punchy sound and splash resistance.",
							"Electronics", "1799.00", 16,
							"https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=900&q=80"),
					createProduct("Yoga Mat", "Non-slip exercise mat with soft cushioning for home workouts.",
							"Fitness", "699.00", 35,
							"https://images.unsplash.com/photo-1592432678016-e910b452f9a2?auto=format&fit=crop&w=900&q=80"),
					createProduct("Leather Wallet", "Slim leather wallet with card slots and a minimal profile.",
							"Accessories", "799.00", 28,
							"https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=900&q=80"),
					createProduct("Water Bottle", "Stainless steel insulated bottle that keeps drinks hot or cold.",
							"Fitness", "599.00", 48,
							"https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=80"),
					createProduct("Sunglasses", "UV-protected sunglasses with a lightweight everyday frame.",
							"Fashion", "999.00", 26,
							"https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=900&q=80"));

			demoProducts.stream()
					.filter(product -> !productRepository.existsByNameIgnoreCaseAndActiveTrue(product.getName()))
					.forEach(productRepository::save);
		};
	}

	private Product createProduct(String name, String description, String category, String price, Integer stock,
			String imageUrl) {
		Product product = new Product();
		product.setName(name);
		product.setDescription(description);
		product.setCategory(category);
		product.setPrice(new BigDecimal(price));
		product.setStock(stock);
		product.setImageUrl(imageUrl);
		product.setActive(true);
		return product;
	}
}
