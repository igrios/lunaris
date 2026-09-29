package com.lunaris.ansenuza.service.interurban;

public class QrExpiredException extends RuntimeException {
    public QrExpiredException() { super("El pase está vencido o revocado."); }
}
