package com.expilicit.InvoiceCentral.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class JMailSender {
    private final JavaMailSender mailSender;


    @Async
    public void sendEmail(String to, String email) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
            messageHelper.setFrom("princedickson03@gmail.com");
            messageHelper.setSubject("confirm your email");
            messageHelper.setTo(to);
            messageHelper.setText(email);
            mailSender.send(mimeMessage);
            log.info("Email sent succefully to {}:", to);
        }
        catch (MessagingException e){
            log.error("Failed to send emial to {}:", to, e);
            throw new IllegalStateException("failed to send email");
        }

    }

    public void sendEmail(String email, String subject, String body) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
            messageHelper.setFrom("princedickson03@gmail.com");
            messageHelper.setSubject(subject);
            messageHelper.setTo(email);
            messageHelper.setText(body, false);
            mailSender.send(mimeMessage);
            log.info("Email sent succefully to {}:", email);
        }
        catch (MessagingException e){
            log.error("Failed to send emial to {}:", email, e);
            throw new IllegalStateException("failed to send email");
        }

    }
}
