package com.lunaris.ansenuza.application.port;

/**
 * Puerto de salida para persistir el PDF de la factura que sube la operadora.
 * La capa de aplicación no conoce el sistema de archivos concreto.
 */
public interface InvoiceStoragePort {

    /** Guarda el contenido del PDF y devuelve su ubicación web y absoluta. */
    StoredInvoice store(byte[] content, String desiredFileName);

    /** Resuelve la ruta absoluta en disco a partir de la URL web guardada (para reenviar). */
    String resolveAbsolutePath(String pdfUrl);

    /** Recupera el PDF persistido para servirlo con headers HTTP controlados. */
    byte[] load(String pdfUrl);

    record StoredInvoice(String webUrl, String absolutePath) {
    }
}
