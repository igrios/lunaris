package com.lunaris.ansenuza.application.port;

/**
 * Botón interactivo de respuesta rápida, agnóstico del canal de mensajería.
 * El adaptador de salida (ej: WhatsApp) lo traduce al formato del proveedor.
 */
public record Button(String id, String title) {
}
