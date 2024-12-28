package com.expilicit.InvoiceCentral.Repository;

import com.expilicit.InvoiceCentral.Dto.LoginAttemptStatus;
import com.expilicit.InvoiceCentral.Entity.LoginAttempt;
import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    Optional<LoginAttempt> findFirstByUserAndStatusInOrderByAttemptDesc(UserRegistration user, List<LoginAttemptStatus> statuses);
}
