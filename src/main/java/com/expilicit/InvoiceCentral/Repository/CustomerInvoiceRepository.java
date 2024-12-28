package com.expilicit.InvoiceCentral.Repository;

import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerInvoiceRepository extends JpaRepository<UserRegistration, Long>{
    Optional<UserRegistration>  findByEmail(String email);

    @Modifying
    @Query("UPDATE UserRegistration u SET u.failedAttempt = :attempts WHERE u.email = :email")
    void updateFailedAttempts(@Param("email") String email, @Param("attempts") int attempts);
}
