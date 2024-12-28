package com.expilicit.InvoiceCentral.Service;

import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Exception.EmailSendingException;
import com.expilicit.InvoiceCentral.Repository.CustomerInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final Random random = new SecureRandom(SecureRandom.getSeed(10));
    private final JMailSender mailSender;
    private final int CODE_LENGTH = 6;
    private final int CODE_EXPIRY_MINUTES = 5;

    @Transactional
    public boolean verifyCode(UserRegistration user, String code) {

        // verify code
        if (user.getTwoFactorCode() == null || user.getTwoFactorCodeExpiry() == null){
            log.warn(" 2Fa code found for user: {}",user.getEmail());
            return false;
        }
        if (LocalDateTime.now().isAfter(user.getTwoFactorCodeExpiry())){
            log.warn(" 2Fa code expire for user: {}", user.getEmail());
            return false;
        }
        if (!user.getTwoFactorCode().equals(code)){
            log.warn(" Invalid 2Fa code provided for user: {}", user.getEmail());
            return false;
        }
        // clear code after verification

        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        customerInvoiceRepository.save(user);
        return true;
    }

    @Transactional
    public void enableTwoFactor(UserRegistration user) {
        user.setTwoFactorEnable(true);
        customerInvoiceRepository.save(user);
        log.info(" 2Fa enable for user: {}", user.getEmail());

    }

    @Transactional
    public void disableTwoFactor(UserRegistration user) {
        user.setTwoFactorEnable(false);
        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        customerInvoiceRepository.save(user);
        log.info(" 2Fa disable for user: {}", user.getEmail());
    }

    public void generateAndSendCode(UserRegistration user) {
        String code = generateCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

        user.setTwoFactorCode(code);
        user.setTwoFactorCodeExpiry(expiry);
        customerInvoiceRepository.save(user);
        sendTwoFactorCode(user.getEmail(), code);
    }

    private String generateCode() {
        StringBuilder code  = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++){
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    private void sendTwoFactorCode(String email, String code) {
        try {
            String subject = "Your Authentication code";
            String body = String.format(
                    "Your verification code is: %s\n" +
                            "This code will expire in %d minutes.\n" +
                            "If you didn't request this code, please ignore this email.",
                    code, CODE_EXPIRY_MINUTES
            );
            mailSender.sendEmail(email, subject,body);
            log.info("2Fa code sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send 2FA code to: {}", email, e);
            throw new EmailSendingException(" failed to send verification code");
        }
    }
}






