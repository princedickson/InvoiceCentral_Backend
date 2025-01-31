package com.expilicit.InvoiceCentral.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,unique = true)
    private String token;
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private UserRegistration user;

    public RefreshToken() {

    }
}
