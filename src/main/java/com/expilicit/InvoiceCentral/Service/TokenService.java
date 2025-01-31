package com.expilicit.InvoiceCentral.Service;

import com.expilicit.InvoiceCentral.AuthProvider.JwtAuthProvider;
import com.expilicit.InvoiceCentral.Dto.TokenPair;
import com.expilicit.InvoiceCentral.Entity.RefreshToken;
import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Exception.TokenExpiredException;
import com.expilicit.InvoiceCentral.Repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor

public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtAuthProvider jwtAuthProvider;

    public TokenPair generateTokenPair(UserRegistration user) {
        String accessToken = jwtAuthProvider.generateToken(user);
        RefreshToken refreshToken = generateRefreshToken(user);

        return new TokenPair(accessToken, refreshToken.getToken());
    }
    public TokenPair refreshAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()->new TokenExpiredException("invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token expire");
        }

        // generate new access token
        String accessToken = jwtAuthProvider.generateToken(token.getUser());

        RefreshToken newRefreshToken = generateRefreshToken(token.getUser());
        refreshTokenRepository.delete(token);
        return new TokenPair(accessToken, newRefreshToken.getToken());
    }

    private RefreshToken generateRefreshToken(UserRegistration user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }


}
