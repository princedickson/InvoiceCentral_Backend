package com.expilicit.InvoiceCentral.AuthProvider;

import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j

public class JwtAuthProvider {

    private final static Logger logger = LoggerFactory.getLogger(JwtAuthProvider.class);

    @Value("${jwt.secret}")

    private String jwtSecret;
    @Value("${jwt.expiration.time}")
    private int jwtExpirationTime;

    public String generateToken(UserRegistration userDetails) {

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtExpirationTime);
        return Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSignatureKey(), SignatureAlgorithm.HS512)
                .claim("userId", userDetails.getId())
                .claim("username", userDetails.getUsername())
                .claim("role", userDetails.getAppRole().name())
                .compact();

    }

    private Key getSignatureKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignatureKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("jwt expired");
        } catch (UnsupportedJwtException e) {
            logger.error("unsupported Jwt token");
        } catch (MalformedJwtException e) {
            logger.error("invalid jwt");
        } catch (SignatureException e) {
            logger.error("invalid signature");
        } catch (IllegalArgumentException e) {
            logger.error("Jwt claim is empty");
        }

        return false;
    }

    // Extract all claims from token
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
