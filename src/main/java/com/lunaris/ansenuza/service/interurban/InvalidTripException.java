package com.lunaris.ansenuza.service.interurban;

public class InvalidTripException extends RuntimeException {
    public InvalidTripException() { super("El pase no está habilitado para este viaje y horario."); }
}
