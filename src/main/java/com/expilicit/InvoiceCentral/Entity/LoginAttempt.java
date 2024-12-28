package com.expilicit.InvoiceCentral.Entity;


import com.expilicit.InvoiceCentral.Dto.LoginAttemptStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime attempt;
    private String ipAddress;
    private String browser;
    private String device;
    private String operatingSystem;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginAttemptStatus status;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private UserRegistration user;


    public LoginAttempt(UserRegistration user, LoginAttemptStatus status, LocalDateTime attempt, String ipAddress,
                        String device, String browser, String operatingSystem) {
        this.user = user;
        this.status = status;
        this.attempt = attempt;
        this.ipAddress = ipAddress;
        this.device = device;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
    }
}
