package com.lunaris.ansenuza.service.interurban;

public class InvalidQrException extends RuntimeException {
    public InvalidQrException() { super("Formato de pase inválido."); }
}
