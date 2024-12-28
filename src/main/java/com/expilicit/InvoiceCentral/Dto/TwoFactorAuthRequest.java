package com.expilicit.InvoiceCentral.Dto;

public record TwoFactorAuthRequest(
        String email,
        String code) {
}
