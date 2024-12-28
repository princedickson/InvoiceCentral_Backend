package com.expilicit.InvoiceCentral.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@Data

public class UserRegistration implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_invoice_sequence")
    @SequenceGenerator(
            sequenceName = "customer_invoice_sequence",
            name = "customer_invoice_sequence",
            allocationSize = 1)
    private Long Id;

    @NotBlank
    private String username;
    @NotBlank
    @Email(message = "please provide valid email")
    private String email;
    @NotBlank
    private String password;
    private LocalDateTime lockTime = null;
    private boolean accountNonLocked = true;
    private int failedAttempt = 0;

    /* 2fa enable */
    private boolean twoFactorEnable = false;
    private String twoFactorCode;
    private LocalDateTime twoFactorCodeExpiry;

    @Enumerated(EnumType.STRING)
    private AppRole appRole = AppRole.USER;
    //private Boolean locked = false;
    private Boolean enable = false;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userRegistration", orphanRemoval = true)
    private List<ConfirmationToken> confirmationTokenList = new ArrayList<>();

    public UserRegistration() {
        this.failedAttempt = 0;
        this.accountNonLocked = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + appRole.name()));
        appRole.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .forEach(authorities::add);
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enable;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AppRole getAppRole() {
        return appRole;
    }

    public void setAppRole(AppRole appRole) {
        this.appRole = appRole;
    }

    public void incrementFailedAttempts() {
        this.failedAttempt++;
    }
}
