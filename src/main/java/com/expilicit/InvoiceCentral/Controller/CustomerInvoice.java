package com.expilicit.InvoiceCentral.Controller;

import com.expilicit.InvoiceCentral.Dto.AccountLoginRequest;
import com.expilicit.InvoiceCentral.Dto.LoginResponse;
import com.expilicit.InvoiceCentral.Dto.ReigisterRequest;
import com.expilicit.InvoiceCentral.Dto.TwoFactorAuthRequest;
import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Service.CustomerLoginService;
import com.expilicit.InvoiceCentral.Service.CustomerRegistrationService;
import com.expilicit.InvoiceCentral.Service.TwoFactorAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/register")
@AllArgsConstructor
public class CustomerInvoice {

    private final CustomerRegistrationService customerRegistrationService;
    private final CustomerLoginService customerLoginService;
    private final TwoFactorAuthService twoFactorAuthService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String Signup( @Valid @RequestBody ReigisterRequest registerRequest){
    return customerRegistrationService.register(registerRequest);
    }
    @GetMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public String VerifyAccount(@RequestParam("token") String token){
        return customerRegistrationService.confirmToken(token);
    }

    @PostMapping("/login")
    public LoginResponse accountLogin(@Valid @RequestBody AccountLoginRequest loginRequest, HttpServletRequest request){
        return customerLoginService.accountLogin(loginRequest, request);
    }

    @PostMapping("/verify-2fa")
    public LoginResponse verifyTwoFactor (@Valid @RequestBody TwoFactorAuthRequest twoFactorAuthRequest){
        return customerLoginService.verifyTwoFactor(twoFactorAuthRequest);
    }

    @PostMapping("/enable-2fa")
    @PreAuthorize("isAuthenticated")
    public ResponseEntity<String> enableTwoFactor(@AuthenticationPrincipal UserRegistration user){
        twoFactorAuthService.enableTwoFactor(user);
        return ResponseEntity.ok("Two-Factor authentication enable ");
    }

    public ResponseEntity<String> disableTwoFactor(@AuthenticationPrincipal UserRegistration user){
        twoFactorAuthService.disableTwoFactor(user);
        return ResponseEntity.ok(" Two-Factor authentication disable");
    }
}
