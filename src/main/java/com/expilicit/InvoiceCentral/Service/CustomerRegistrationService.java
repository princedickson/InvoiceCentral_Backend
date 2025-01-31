package com.expilicit.InvoiceCentral.Service;


import com.expilicit.InvoiceCentral.Dto.ReigisterRequest;
import com.expilicit.InvoiceCentral.Exception.*;
import com.expilicit.InvoiceCentral.Entity.AppRole;
import com.expilicit.InvoiceCentral.Entity.ConfirmationToken;
import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Repository.ConfirmationTokenRepository;
import com.expilicit.InvoiceCentral.Repository.CustomerInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(CustomerRegistrationService.class);
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final JMailSender mailSender;

    @Transactional
    public String register(ReigisterRequest registerRequest) {

        validateRegistrationRequest(registerRequest);

        // check if email already exist
        if (customerInvoiceRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new UserAlreadyExistsException("This user already exist");
        }

        // create user info
        UserRegistration userRegistration = createUserRegistration(registerRequest);

        // generate and save confirmationToken
        ConfirmationToken confirmationToken = generateConfirmationToken(userRegistration);

        String link = generateConfirmationLink(confirmationToken.getToken());
        sendConfirmationEmail(registerRequest.email(), registerRequest.username(), link);
        return "user register successfully please check your email to confirm your account";
    }

    private void validateRegistrationRequest(ReigisterRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidRegistrationException("Password cannot be empty");
        }

        if (request.email() == null || !isValidEmail(request.email())) {
            throw new InvalidRegistrationException("Invalid email address");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    // user Information
    private UserRegistration createUserRegistration(ReigisterRequest registerRequest) {
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setUsername(registerRequest.username());
        userRegistration.setEmail(registerRequest.email());
        userRegistration.setPassword(passwordEncoder.encode(registerRequest.password()));
        userRegistration.setAppRole(AppRole.USER);
        userRegistration.setEnabled(false);
        userRegistration.setAccountNonLocked(true);
        return customerInvoiceRepository.save(userRegistration);
    }

    @Transactional
    public String confirmToken(String token) {

        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("invalid token"));

        validateToken(confirmationToken);
        activateUser(confirmationToken);

        return "Account successfully confirmed";
    }

    // confirmationToken
    private ConfirmationToken generateConfirmationToken(UserRegistration userRegistration) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                userRegistration
        );
        return confirmationTokenRepository.save(confirmationToken);
    }

    private void sendConfirmationEmail(String email, String username, String link) {

        try {
            mailSender.sendEmail(email, buildEmail(username, link));
            log.info("Confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to: {}", email, e);
            throw new EmailSendingException("Failed to send confirmation email");
        }
    }

    private String generateConfirmationLink(String token) {
        final String url = "http://localhost:4200/verify?token=";
        return url + token;
    }

    private void validateToken(ConfirmationToken confirmationToken) {
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Token already confirmed");
        }
        if (LocalDateTime.now().isAfter(confirmationToken.getExpireAt())) {
            confirmationTokenRepository.delete(confirmationToken);
            throw new TokenExpiredException(" Token is expired");
        }
    }

    private void activateUser(ConfirmationToken confirmationToken) {
        UserRegistration userRegistration = confirmationToken.getUserRegistration();
        userRegistration.setEnabled(true);
        customerInvoiceRepository.save(userRegistration);
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
        log.info("user {} activated successfully", userRegistration.getEmail());
    }

    private String buildEmail(String name, String link) {
        final String formatted = String.format(
                "Hi %s,\n\n" +
                        "Please click the link below to verify your account:\n" +
                        "%s\n\n" +
                        "The link will expire in 15 minutes.\n\n" +
                        "Best regards,\n" +
                        "Your Application Team",
                name, link
        );
        return formatted;
    }
}
