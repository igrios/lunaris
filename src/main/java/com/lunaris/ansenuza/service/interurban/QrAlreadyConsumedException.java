package com.lunaris.ansenuza.service.interurban;

public class QrAlreadyConsumedException extends RuntimeException {
    public QrAlreadyConsumedException() { super("El pase ya fue utilizado."); }
}
