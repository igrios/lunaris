package com.lunaris.ansenuza.domain.exception;

public class InquiryNotFoundException extends RuntimeException {
    public InquiryNotFoundException() {
        super("No se encontró la consulta solicitada.");
    }
}
