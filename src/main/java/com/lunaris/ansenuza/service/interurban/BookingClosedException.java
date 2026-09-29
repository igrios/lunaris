package com.lunaris.ansenuza.service.interurban;

public class BookingClosedException extends RuntimeException {
    public BookingClosedException() { super("El viaje está cerrado para nuevas reservas."); }
}
