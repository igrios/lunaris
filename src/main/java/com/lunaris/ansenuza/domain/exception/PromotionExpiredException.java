package com.lunaris.ansenuza.domain.exception;

public class PromotionExpiredException extends IllegalArgumentException {

    public PromotionExpiredException() {
        super("El código ingresado ha expirado");
    }
}
