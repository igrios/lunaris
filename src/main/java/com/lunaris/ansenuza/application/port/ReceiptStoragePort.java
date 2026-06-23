package com.lunaris.ansenuza.application.port;

/**
 * Puerto de salida para descargar y persistir el comprobante de pago enviado
 * por el pasajero. Devuelve la URL web local del comprobante, o {@code null}
 * si la descarga falló.
 */
public interface ReceiptStoragePort {

    String downloadAndSaveReceipt(String mediaId);
}
