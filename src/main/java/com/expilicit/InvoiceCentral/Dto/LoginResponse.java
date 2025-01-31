package com.expilicit.InvoiceCentral.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT token for authentication. Null if 2FA is required")
        String accessToken,

        @Schema(description = "Refresh token for getting new access tokens. Null if 2FA is required")
        String refreshToken,

        @Schema(description = "Indicates if 2FA verification is required")
        boolean requiresTwoFactor,

        @Schema(description = "Response message")
        String message
) {
        public static LoginResponse twoFactorRequired(String message) {
                return new LoginResponse(null, null, true, message);
        }

        public static LoginResponse successful(String accessToken, String refreshToken, String message) {
                return new LoginResponse(accessToken, refreshToken, false, message);
        }
}
