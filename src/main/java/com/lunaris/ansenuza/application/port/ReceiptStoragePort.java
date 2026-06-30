package com.lunaris.ansenuza.application.port;

import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de salida para descargar y persistir el comprobante de pago enviado
 * por el pasajero. Devuelve la URL web local o de Cloudinary del comprobante, 
 * o {@code null} si la descarga falló.
 */
public interface ReceiptStoragePort {

    // Tu método original del Bot (Mantenido intacto)
    String downloadAndSaveReceipt(String mediaId);

    // 🔥 NUEVO: Método para que Martín suba archivos desde el formulario web
    String uploadFile(MultipartFile file);
}