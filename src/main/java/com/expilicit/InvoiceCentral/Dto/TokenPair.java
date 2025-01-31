package com.expilicit.InvoiceCentral.Dto;

import jakarta.validation.constraints.NotNull;

public record TokenPair(
        @NotNull
        String accessToken,

        @NotNull
        String refreshToken
) {
}
