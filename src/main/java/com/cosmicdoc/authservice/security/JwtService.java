package com.cosmicdoc.authservice.security;

import com.cosmicdoc.common.model.OrganizationMember;
import com.cosmicdoc.common.model.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final Key secretKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret-key}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secret.getBytes()));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT for a user belonging to a specific organization.
     */
    public String generateToken(Users user, OrganizationMember membership) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("email", user.getEmail())
                .claim("organizationId", membership.getOrganizationId())
                .claim("role", membership.getRole()) // The user's role in THAT organization
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
