package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	private Instant passwordUpdatedAt;

	@Column(nullable = false)
	private Integer passwordChangeCount = 0;

	@Column(nullable = false)
	private String role = "USER";

	private String phone;

	private String alternatePhone;

	private String profilePictureUrl;

	@Lob
	@Column(columnDefinition = "LONGBLOB")
	private byte[] profileImage;

	private String profileImageContentType;

	private String profileImageFileName;

	private Instant profileImageUpdatedAt;

	@Column(length = 600)
	private String address;
}
