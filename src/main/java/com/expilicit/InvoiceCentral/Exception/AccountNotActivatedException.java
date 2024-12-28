package com.expilicit.InvoiceCentral.Exception;

public class AccountNotActivatedException extends RuntimeException{
    public AccountNotActivatedException(String message) {
        super(message);
    }
}
