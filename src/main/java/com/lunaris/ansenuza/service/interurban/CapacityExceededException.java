package com.lunaris.ansenuza.service.interurban;

public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException() { super("No hay plazas en todos los tramos solicitados."); }
}
