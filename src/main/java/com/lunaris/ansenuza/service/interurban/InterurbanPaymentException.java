package com.lunaris.ansenuza.service.interurban;

/** Mensajes sanitizados: nunca incluir credenciales, tokens ni respuesta del proveedor. */
public class InterurbanPaymentException extends RuntimeException {
    public enum Code { INVALID_SIGNATURE, INVALID_NOTIFICATION, PROVIDER_UNAVAILABLE, QR_FAILURE }
    private final Code code;

    public InterurbanPaymentException(Code code) {
        super(code.name());
        this.code = code;
    }

    public Code code() { return code; }
}
