package com.chainerp.backend.api;

import com.chainerp.backend.config.SecurityProperties;
import com.chainerp.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;

    public AuthController(
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService,
        JwtService jwtService,
        SecurityProperties securityProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
    }

    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        if (request.username() == null || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password are required");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        long expiresInSeconds = securityProperties.getAccessTokenMinutes() * 60;
        return new TokenResponse(jwtService.generateToken(user), "Bearer", expiresInSeconds);
    }
}
