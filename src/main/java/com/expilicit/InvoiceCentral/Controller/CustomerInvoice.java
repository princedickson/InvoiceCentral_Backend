package com.expilicit.InvoiceCentral.Controller;

import com.expilicit.InvoiceCentral.Dto.AccountLoginRequest;
import com.expilicit.InvoiceCentral.Dto.LoginResponse;
import com.expilicit.InvoiceCentral.Dto.ReigisterRequest;
import com.expilicit.InvoiceCentral.Dto.TwoFactorAuthRequest;
import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Service.CustomerLoginService;
import com.expilicit.InvoiceCentral.Service.CustomerRegistrationService;
import com.expilicit.InvoiceCentral.Service.TwoFactorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class CustomerInvoice {

    private final CustomerRegistrationService customerRegistrationService;
    private final CustomerLoginService customerLoginService;
    private final TwoFactorAuthService twoFactorAuthService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String signup(@Valid @RequestBody ReigisterRequest registerRequest) {
        return customerRegistrationService.register(registerRequest);
    }

    @GetMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public String verifyAccount(@RequestParam("token") String token) {
        return customerRegistrationService.confirmToken(token);
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates user with email and password. Returns JWT token or triggers 2FA if enabled."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login succesfull",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })

    @PostMapping("/login")
    public LoginResponse accountLogin(@Valid @RequestBody AccountLoginRequest loginRequest, HttpServletRequest request) {
        return customerLoginService.accountLogin(loginRequest, request);
    }

    @Operation(
            summary = "Verify Two-Factor Authentication",
            description = "Verifies the 2FA code sent to user's email"
    )
    @PostMapping("/verify-2fa")
    public LoginResponse verifyTwoFactor(@Valid @RequestBody TwoFactorAuthRequest twoFactorAuthRequest) {
        return customerLoginService.verifyTwoFactor(twoFactorAuthRequest);
    }

    @PostMapping("/enable-2fa")
    @PreAuthorize("isAuthenticated")
    public ResponseEntity<String> enableTwoFactor(@AuthenticationPrincipal UserRegistration user) {
        twoFactorAuthService.enableTwoFactor(user);
        return ResponseEntity.ok("Two-Factor authentication enable ");
    }

    public ResponseEntity<String> disableTwoFactor(@AuthenticationPrincipal UserRegistration user) {
        twoFactorAuthService.disableTwoFactor(user);
        return ResponseEntity.ok(" Two-Factor authentication disable");
    }
}
