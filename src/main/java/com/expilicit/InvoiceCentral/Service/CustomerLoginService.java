package com.expilicit.InvoiceCentral.Service;

import com.expilicit.InvoiceCentral.AuthProvider.JwtAuthProvider;
import com.expilicit.InvoiceCentral.Dto.AccountLoginRequest;
import com.expilicit.InvoiceCentral.Dto.LoginAttemptStatus;
import com.expilicit.InvoiceCentral.Dto.LoginResponse;
import com.expilicit.InvoiceCentral.Dto.TwoFactorAuthRequest;
import com.expilicit.InvoiceCentral.Entity.LoginAttempt;
import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Exception.EmailSendingException;
import com.expilicit.InvoiceCentral.Repository.CustomerInvoiceRepository;
import com.expilicit.InvoiceCentral.Repository.LoginAttemptRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerLoginService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKED_DURATION_TIME = 15;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthProvider jwtAuthProvider;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JMailSender mailSender;
    private final TwoFactorAuthService twoFactorAuthService;

    @Transactional
    public LoginResponse accountLogin(AccountLoginRequest loginRequest, HttpServletRequest request) {
        try {
            UserRegistration user = customerInvoiceRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));

            log.info("Found user {} with current failed attempts: {}",
                    user.getEmail(), user.getFailedAttempt());


            // Parse user agent and IP address
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = extractIpAddress(request);
            Client clientDetails = parseUserAgent(userAgent);

            if (isAccountLocked(user)) {
                throw new LockedException("Account is locked please wait or contact support");
            }

            if (user.isTwoFactorEnable()){
                twoFactorAuthService.generateAndSendCode(user);
                return new LoginResponse(null, true, " 2Fa code sent to your email");
            }

            try {
                Authentication authentication = authenticationManager.authenticate
                        (new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

                checkAndNotifyNewDeviceLocation(user, ipAddress, clientDetails);
                resetFailedLoginAttempt(user);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String token = jwtAuthProvider.generateToken(user);
                return new LoginResponse(token, false, "Login successfully");
                //UserRegistration userDetails = (UserRegistration) authentication.getPrincipal();
                //return jwtAuthProvider.generateToken(userDetails);
            } catch (AuthenticationException e) {
                handleFailedLogin(user,request);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, " invalid credential");
            }

        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }
    }

    private void checkAndNotifyNewDeviceLocation(UserRegistration user, String currenttIpAddress, Client currentClient) {

        // find the recent login attempt for a user
        Optional<LoginAttempt> recentLoginAttempt = loginAttemptRepository.findFirstByUserAndStatusInOrderByAttemptDesc(
                user, Arrays.asList(LoginAttemptStatus.values())
        );

        /* if the first login attempt or a different device location */
        if (recentLoginAttempt.isEmpty() || isNewDeviceOrLocation(recentLoginAttempt.get(), currenttIpAddress, currentClient)){
            NotifyNewDeviceLocation(user, currenttIpAddress, currentClient);
        }
    }

    private boolean isNewDeviceOrLocation(LoginAttempt lastLogin, String currenttIpAddress, Client currentClient) {

        // compare current login details with the last successfully login

        return !lastLogin.getIpAddress().equals(currenttIpAddress) ||
                !lastLogin.getDevice().equals(currentClient.device.family) ||
                !lastLogin.getBrowser().equals(currentClient.userAgent.family) ||
                !lastLogin.getOperatingSystem().equals(currentClient.os.family);

    }

    private void NotifyNewDeviceLocation(UserRegistration user, String ipAddress, Client clientDetails) {
        try {
            String subject = "New Device Login Detected";
            String body = String.format(
                    "Dear %s,\n\n" +
                            "A new login to your account has been detected:\n" +
                            "Device: %s\n" +
                            "Browser: %s\n" +
                            "Operating System: %s\n" +
                            "IP Address: %s\n\n" +
                            "If this was not you, please contact support immediately.",
                    user.getUsername(),
                    clientDetails.device != null ? clientDetails.device.family : "Unknown Device",
                    clientDetails.userAgent != null ? clientDetails.userAgent.family : "Unknown Browser",
                    clientDetails.os != null ? clientDetails.os.family : "Unknown OS",
                    ipAddress
            );

            mailSender.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Failed to send new device login notification", e);
        }
    }

    private boolean isAccountLocked(UserRegistration user) {
        if (!user.isAccountNonLocked()) {

            LocalDateTime lockTime = user.getLockTime();
            if (lockTime == null){
                unLockUserAccount(user);
                return false;
            }
            LocalDateTime unLockTime = lockTime.plusMinutes(LOCKED_DURATION_TIME);

            if (LocalDateTime.now().isBefore(unLockTime)) {
                return true;
            } else {
                unLockUserAccount(user);
            }
        }
        return false;
    }

    private void resetFailedLoginAttempt(UserRegistration user) {
        user.setLockTime(null);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        customerInvoiceRepository.save(user);
    }

    @Transactional
    private void handleFailedLogin(UserRegistration user, HttpServletRequest request) {
        recordFailedLoginAttempt(user, request, LoginAttemptStatus.FAILED);

        log.info("Current failed attempts before increment for user {}: {}",
                user.getEmail(), user.getFailedAttempt());

        incrementFailedAttempts(user);

        log.info("Failed attempts after increment for user {}: {}",
                user.getEmail(), user.getFailedAttempt());

        if (user.getFailedAttempt() >= MAX_FAILED_ATTEMPTS) {
            log.info("Maximum failed attempts ({}) reached for user {}, locking account",
                    MAX_FAILED_ATTEMPTS, user.getEmail());
            lockAccount(user);
        }
        customerInvoiceRepository.save(user);
    }

    @Transactional
    private void lockAccount(UserRegistration user) {
        log.info("Locking account for user: {}", user.getEmail());
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
        customerInvoiceRepository.save(user);
        log.info("Account locked, sending notification email to user: {}", user.getEmail());
        notifyUserAccountLocked(user);
        log.info("Lock notification email sent to user: {}", user.getEmail());
    }

    private void unLockUserAccount(UserRegistration user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        customerInvoiceRepository.save(user);
    }

    @Transactional
    private void incrementFailedAttempts(UserRegistration user) {
        int newAttempts = user.getFailedAttempt() + 1;
        customerInvoiceRepository.updateFailedAttempts(user.getEmail(), newAttempts);
        user.setFailedAttempt(newAttempts);

        log.info("Updated failed attempts for user {} to {}", user.getEmail(), newAttempts);
    }

    private void recordFailedLoginAttempt(UserRegistration user, HttpServletRequest request, LoginAttemptStatus status) {
        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        Client clientDetails = parseUserAgent(userAgent);
        final String unknown = "Unknown";
        LoginAttempt attempt = new LoginAttempt(
                user,
                status,
                LocalDateTime.now(),
                ipAddress,
                clientDetails.device != null ? clientDetails.device.family: unknown,
                clientDetails.userAgent != null ? clientDetails.userAgent.family: unknown,
                clientDetails.os != null ? clientDetails.os.family: unknown
        );
        loginAttemptRepository.save(attempt);
        log.info("Login attempt recorded for user: {}", user.getEmail());
    }

    private Client parseUserAgent(String userAgent) {
        try {
            Parser parser = new Parser();
            return parser.parse(userAgent);
        } catch (Exception e) {
            return new Client(null, null, null);
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forward-For");
        if (StringUtils.isNotEmpty(ipAddress) && ipAddress.contains(",")) {
            return ipAddress.split(", ")[0];
        }
        return StringUtils.defaultIfEmpty(ipAddress, request.getRemoteAddr());
    }

    private void notifyUserAccountLocked(UserRegistration user) {
        try {
            log.info("Preparing to send lock notification email to user: {}", user.getEmail());
            String subject = "Account security alert";
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your account has been temporarily locked due to multiple failed login attempts. " +
                            "The lock will be lifted in %d minutes.\n\n" +
                            "If you did not attempt these logins, please contact support immediately.",
                    user.getUsername(), LOCKED_DURATION_TIME
            );
            mailSender.sendEmail(user.getEmail(), subject, body);
            log.info("Lock notification email sent successfully to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send lock notification email to user: {}", user.getEmail(), e);
            throw new EmailSendingException("Failed to send email");
        }
    }

    @Transactional
    public LoginResponse verifyTwoFactor(TwoFactorAuthRequest twoFactorAuthRequest) {
        UserRegistration user = customerInvoiceRepository.findByEmail(twoFactorAuthRequest.email())
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));

        if (!twoFactorAuthService.verifyCode(user, twoFactorAuthRequest.code())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
        }
        String token = jwtAuthProvider.generateToken(user);
        return new LoginResponse(token, false, "2Fa verification successful");
    }
}
