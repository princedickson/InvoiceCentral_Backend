package com.expilicit.InvoiceCentral.Dto;

public record LoginResponse(
        String token,
        boolean requiresTwoFactor,
        String message
) { }
