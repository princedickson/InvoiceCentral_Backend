package com.expilicit.InvoiceCentral.Repository;

import com.expilicit.InvoiceCentral.Entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String refreshToken);

    void delete(RefreshToken token);
}
