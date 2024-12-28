package com.expilicit.InvoiceCentral.Exception;

public class TokenAlreadyConfirmedException extends RuntimeException{
    public TokenAlreadyConfirmedException(String message) {
        super(message);
    }
}
