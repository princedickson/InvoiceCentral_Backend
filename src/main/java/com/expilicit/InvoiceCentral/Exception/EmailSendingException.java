package com.expilicit.InvoiceCentral.Exception;

public class EmailSendingException extends RuntimeException{
    public EmailSendingException(String message) {
        super(message);
    }
}
