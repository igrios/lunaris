package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🧾 Factura emitida para una reserva con pago confirmado.
 * La factura fiscal la arma la operadora por fuera del sistema; acá guardamos el
 * registro, el PDF subido a mano y la marca de envío por WhatsApp.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "invoice_number", length = 40)
    private String invoiceNumber;

    @Column(name = "passenger_name", length = 200)
    private String passengerName;

    @Column(name = "passenger_cuil", length = 20)
    private String passengerCuil;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "pdf_url", length = 300)
    private String pdfUrl;

    @Column(name = "sent_via_whatsapp", nullable = false)
    private Boolean sentViaWhatsapp;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
