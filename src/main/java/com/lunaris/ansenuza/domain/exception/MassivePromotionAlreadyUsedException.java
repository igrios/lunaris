package com.lunaris.ansenuza.domain.exception;

public class MassivePromotionAlreadyUsedException extends IllegalArgumentException {

    public MassivePromotionAlreadyUsedException() {
        super("Esta promoción masiva ya fue utilizada por este número de teléfono");
    }
}
