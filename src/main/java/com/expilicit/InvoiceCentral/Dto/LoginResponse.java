package com.expilicit.InvoiceCentral.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT token for authentication. Null if 2FA is required")
        String token,

        @Schema(description = "Indicates if 2FA verification is required")
        boolean requiresTwoFactor,

        @Schema(description = "Response message")
        String message
) { }
