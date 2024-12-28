package com.expilicit.InvoiceCentral.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConfirmationToken {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "confirmed_token_sequence")
    @SequenceGenerator(sequenceName = "confirmed_token_sequence", name = "confirmed_token_sequence", allocationSize = 1)
    @Id
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime expireAt;
    private LocalDateTime confirmedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserRegistration userRegistration;

    public ConfirmationToken(
            String token,
            LocalDateTime createdAt,
            LocalDateTime expireAt,
            UserRegistration userRegistration) {

        this.token = token;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.confirmedAt = confirmedAt;
        this.userRegistration = userRegistration;
    }
}
