package com.ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

	// Reads value from application.properties
	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	// Converts the string secret into a cryptographic Key object
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	// Called after login/register — creates a new token for the user
	public String generateToken(String email) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder().setSubject(email) // who this token belongs to
				.setIssuedAt(now) // when it was created
				.setExpiration(expiryDate) // when it dies
				.signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign it
				.compact(); // build the final string
	}

	// Reads the email from inside the token
	public String extractEmail(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
	}

	// Checks if token is valid (not expired, not tampered)
	public boolean isTokenValid(String token) {
		try {
			extractEmail(token); // if this throws, token is bad
			return true;
		} catch (ExpiredJwtException e) {
			System.out.println("JWT expired: " + e.getMessage());
			return false;
		} catch (MalformedJwtException e) {
			System.out.println("JWT malformed: " + e.getMessage());
			return false;
		} catch (SignatureException e) {
			System.out.println("JWT signature invalid: " + e.getMessage());
			return false;
		} catch (JwtException e) {
			System.out.println("JWT error: " + e.getMessage());
			return false;
		}
	}
}