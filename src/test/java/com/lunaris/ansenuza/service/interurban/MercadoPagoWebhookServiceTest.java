package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

class MercadoPagoWebhookServiceTest {
    static final String SECRET = "unit-test-webhook-secret";
    static final byte[] AES_KEY = new byte[32]; // Sólo fixture, jamás configuración productiva.
    final Instant now = Instant.parse("2026-09-20T18:00:00Z");
    final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    final UUID bookingId = UUID.randomUUID();
    final UUID reservationId = UUID.randomUUID();
    final InterurbanPaymentRepository.Inbox inbox = new InterurbanPaymentRepository.Inbox(UUID.randomUUID(), "12345");
    final InterurbanPaymentRepository repository = mock(InterurbanPaymentRepository.class);
    final PaymentQueryPort provider = mock(PaymentQueryPort.class);
    final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    MercadoPagoWebhookService service;
    QrGeneratorService qr;

    @BeforeEach void setup() {
        when(transactions.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        var cipher = new QrArtifactCipher(Base64.getEncoder().encodeToString(AES_KEY), "test-key");
        qr = new QrGeneratorService(repository, cipher, clock);
        service = new MercadoPagoWebhookService(new MercadoPagoSignatureValidator(SECRET, clock, Duration.ofHours(24)),
                provider, repository, qr, transactions, "999", false, clock);
        when(repository.pending()).thenReturn(List.of(inbox));
        when(repository.lockPending(inbox.id())).thenReturn(true);
        when(repository.lockBooking(bookingId)).thenReturn(List.of(reservation("HELD", now.plusSeconds(600))));
        when(repository.lockPaidReservation(reservationId)).thenReturn(true);
        when(provider.getPayment("12345")).thenReturn(payment("approved", "100.00", "ARS", "999"));
    }

    InterurbanPaymentRepository.Reservation reservation(String status, Instant expires) {
        return new InterurbanPaymentRepository.Reservation(reservationId, status, new BigDecimal("100.00"),
                expires, now.plusSeconds(7200), true, "OPEN");
    }

    PaymentQueryPort.Payment payment(String status, String amount, String currency, String collector) {
        return new PaymentQueryPort.Payment("12345", status, "INTERURBAN:" + bookingId, new BigDecimal(amount),
                currency, collector, false, BigDecimal.ZERO);
    }

    static String signature(String paymentId, String requestId, String timestamp) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "ts=" + timestamp + ",v1=" + HexFormat.of().formatHex(mac.doFinal(
                ("id:" + paymentId + ";request-id:" + requestId + ";ts:" + timestamp + ";").getBytes(StandardCharsets.UTF_8)));
    }

    static String decodeToken(UUID reservation, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(AES_KEY, "AES"),
                new GCMParameterSpec(128, Arrays.copyOf(encrypted, 12)));
        cipher.updateAAD(reservation.toString().getBytes(StandardCharsets.UTF_8));
        byte[] png = cipher.doFinal(Arrays.copyOfRange(encrypted, 12, encrypted.length));
        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(320, image.getWidth());
        return new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(image)))).getText();
    }

    @Test void approvedNotificationMarksPaidAndEmitsA256BitQrWithMatchingHash() throws Exception {
        service.receive("12345", "request-1", signature("12345", "request-1", Long.toString(now.toEpochMilli())),
                new MercadoPagoWebhookService.Notification("payment", "payment.updated",
                        new MercadoPagoWebhookService.Notification.Data("12345")));
        verify(repository).enqueue(matches("[a-f0-9]{64}"), eq("12345"));
        verifyNoInteractions(provider); // HTTP responde tras inbox; worker consulta API.
        service.processPending();
        verify(provider).getPayment("12345");
        verify(repository).recordPayment(any(), eq(bookingId), eq("APPROVED"));
        var hash = ArgumentCaptor.forClass(String.class);
        var artifact = ArgumentCaptor.forClass(byte[].class);
        var ordered = inOrder(repository);
        ordered.verify(repository).markPaid(reservationId);
        ordered.verify(repository).saveQr(eq(reservationId), hash.capture(), eq(now.plusSeconds(7200)), artifact.capture(), eq("test-key"));
        ordered.verify(repository).complete(inbox.id(), "APPROVED");
        String token = decodeToken(reservationId, artifact.getValue());
        assertEquals(32, Base64.getUrlDecoder().decode(token).length);
        assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8))), hash.getValue());
        assertFalse(token.contains(reservationId.toString()));
    }

    @Test void duplicatePaymentDoesNotRegenerateQr() {
        when(repository.paymentStatus("12345")).thenReturn(Optional.of("APPROVED"));
        when(repository.lockBooking(bookingId)).thenReturn(List.of(reservation("PAID", now.plusSeconds(600))));
        service.process(inbox);
        verify(repository).complete(inbox.id(), "ALREADY_APPROVED");
        verify(repository, never()).markPaid(any());
        verify(repository, never()).saveQr(any(), any(), any(), any(), any());
    }

    @Test void invalidSignatureNeverEnqueuesOrQueries() {
        assertThrows(InterurbanPaymentException.class, () -> service.receive("12345", "request-1", "bad", null));
        verifyNoInteractions(repository, provider);
    }

    @Test void signedQueryCannotBeReplacedByUnsignedBodyId() throws Exception {
        assertThrows(InterurbanPaymentException.class, () -> service.receive("12345", "request-1",
                signature("12345", "request-1", Long.toString(now.getEpochSecond())),
                new MercadoPagoWebhookService.Notification("payment", "payment.updated",
                        new MercadoPagoWebhookService.Notification.Data("67890"))));
        verifyNoInteractions(repository, provider);
    }

    @Test void wrongAmountDoesNotConfirm() {
        when(provider.getPayment("12345")).thenReturn(payment("approved", "99.99", "ARS", "999"));
        service.process(inbox);
        verify(repository).review("12345", "AMOUNT_MISMATCH");
        verify(repository, never()).markPaid(any());
    }

    @Test void wrongCollectorDoesNotConfirm() {
        when(provider.getPayment("12345")).thenReturn(payment("approved", "100.00", "ARS", "888"));
        service.process(inbox);
        verify(repository).review("12345", "PAYMENT_IDENTITY_MISMATCH");
        verify(repository, never()).markPaid(any());
    }

    @Test void pendingPaymentCanBeApprovedByLaterNotification() {
        when(provider.getPayment("12345")).thenReturn(payment("pending", "100", "ARS", "999"));
        service.process(inbox);
        verify(repository).complete(inbox.id(), "NOT_APPROVED");
        verify(repository, never()).markPaid(any());
        when(provider.getPayment("12345")).thenReturn(payment("approved", "100", "ARS", "999"));
        service.process(new InterurbanPaymentRepository.Inbox(inbox.id(), "12345"));
        verify(repository).markPaid(reservationId);
    }

    @Test void expiredHoldIsReviewedWithoutRecreatingCapacity() {
        when(repository.lockBooking(bookingId)).thenReturn(List.of(reservation("HELD", now)));
        service.process(inbox);
        verify(repository).review("12345", "BOOKING_NOT_PAYABLE");
        verify(repository, never()).markPaid(any());
    }

    @Test void networkFailureRemainsPendingForRetry() {
        when(provider.getPayment("12345")).thenThrow(new InterurbanPaymentException(InterurbanPaymentException.Code.PROVIDER_UNAVAILABLE));
        service.processPending();
        verify(repository).retry(inbox.id());
        verify(repository, never()).complete(any(), any());
    }

    @Test void renderingPersistenceFailureRollsBackConfirmation() {
        doThrow(new RuntimeException("fixture")).when(repository).saveQr(any(), any(), any(), any(), any());
        assertThrows(InterurbanPaymentException.class, () -> service.process(inbox));
        verify(transactions).rollback(any());
        verify(transactions, never()).commit(any());
        verify(repository, never()).complete(any(), any());
    }

    @Test void refundRevokesPreviouslyApprovedQrWithoutReissuing() {
        when(repository.paymentStatus("12345")).thenReturn(Optional.of("APPROVED"));
        when(provider.getPayment("12345")).thenReturn(payment("refunded", "100", "ARS", "999"));
        service.process(inbox);
        verify(repository).revokeBooking(bookingId, now);
        verify(repository).recordPayment(any(), eq(bookingId), eq("REVOKED"));
        verify(repository, never()).saveQr(any(), any(), any(), any(), any());
    }

    @Test void staleApprovalCannotResurrectRevokedPayment() {
        when(repository.paymentStatus("12345")).thenReturn(Optional.of("REVOKED"));
        service.process(inbox);
        verify(repository).complete(inbox.id(), "ALREADY_REVOKED");
        verify(repository, never()).markPaid(any());
    }

    @Test void repeatedQrGenerationPreservesOriginalToken() {
        when(repository.hasQr(reservationId)).thenReturn(true);
        qr.generate(reservationId, now.plusSeconds(600));
        verify(repository, never()).saveQr(any(), any(), any(), any(), any());
    }
}
