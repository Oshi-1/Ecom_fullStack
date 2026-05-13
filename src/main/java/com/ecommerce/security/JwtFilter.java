package com.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
	// OncePerRequestFilter guarantees this runs ONCE per request, not multiple
	// times

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Step 1: Read the Authorization header
		// Frontend sends: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
		String authHeader = request.getHeader("Authorization");

		String token = null;
		String email = null;

		// Step 2: Extract the token if header exists and starts with "Bearer "
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7); // remove "Bearer " prefix

			// Step 3: Extract email from token (if token is valid)
			if (jwtUtil.isTokenValid(token)) {
				email = jwtUtil.extractEmail(token);
			}
		}

		// Step 4: If we got an email AND user is not already authenticated
		// (SecurityContextHolder.getContext().getAuthentication() == null means
		// this request hasn't been authenticated yet in this cycle)
		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			// Step 5: Load full user details from database
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			// Step 6: Create authentication object
			// This is the object Spring Security uses to know "who is making this request"
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, // who
																														// (user
																														// details)
					null, // credentials (null because JWT already verified)
					userDetails.getAuthorities() // what they can do (ROLE_USER, etc.)
			);

			// Step 7: Attach request details (IP address, session info)
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			// Step 8: Tell Spring Security "this request is authenticated"
			// From this point, any @AuthenticationPrincipal will work in controllers
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		// Step 9: Always continue the filter chain
		// If token was invalid, we just don't set authentication
		// Spring Security will then reject the request if the route requires auth
		filterChain.doFilter(request, response);
	}
}