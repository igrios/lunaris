package com.lunaris.ansenuza.application.conversation.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.service.interurban.InterurbanCatalog;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;

class InterurbanBotRoutingTest {
    private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
    private final LocalityRepository localities = mock(LocalityRepository.class);
    private final PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
    private final MessagingPort messaging = mock(MessagingPort.class);
    private final InterurbanCatalog catalog = mock(InterurbanCatalog.class);
    private final ConversationSession session = ConversationSession.builder()
            .phoneNumber("543511112222").currentStep("ASK_LOCALITY").build();

    private IncomingMessage message(String body) {
        return new IncomingMessage(session.getPhoneNumber(), IncomingMessage.MessageType.TEXT, body, null);
    }

    @Test void enabledRoutesMorterosToBrinkmannWithoutConventionalPricing() {
        when(localities.findAllWithActiveFare()).thenReturn(List.of(Locality.builder().name("Morteros").build()));
        when(catalog.origins()).thenReturn(List.of("Morteros"));
        when(catalog.destinations("Morteros")).thenReturn(List.of(
                new InterurbanCatalog.Destination(3, "Brinkmann", new BigDecimal("5000"))));
        var origin = new AskLocalityHandler(sessions, localities, pricing, messaging, Optional.of(catalog));
        origin.handle(session, message("1"));
        assertThat(session.getCurrentStep()).isEqualTo("ASK_INTERURBAN_DESTINATION");
        verify(messaging).sendText(eq(session.getPhoneNumber()), contains("i_3) Brinkmann"));
        var destination = new AskInterurbanDestinationHandler(catalog, sessions, messaging, localities, origin);
        destination.handle(session, message("i_3"));
        assertThat(session.getDestination()).isEqualTo("Brinkmann");
        assertThat(session.getCurrentStep()).isEqualTo("ASK_INTERURBAN_CONFIRMATION");
        assertThat(session.isBotPaused()).isFalse();
        verifyNoInteractions(pricing);
        new InterurbanConfirmationHandler(sessions, messaging).handle(session, message("interurban_reserve"));
        assertThat(session.isBotPaused()).isTrue();
    }

    @Test void originWithOnlyInterurbanFareRemainsSelectable() {
        when(localities.findAllWithActiveFare()).thenReturn(List.of());
        when(catalog.origins()).thenReturn(List.of("Morteros"));
        when(catalog.destinations("Morteros")).thenReturn(List.of(
                new InterurbanCatalog.Destination(3, "Brinkmann", BigDecimal.TEN)));
        new AskLocalityHandler(sessions, localities, pricing, messaging, Optional.of(catalog))
                .handle(session, message("1"));
        assertThat(session.getPickupLocality()).isEqualTo("Morteros");
        verifyNoInteractions(pricing);
    }

    @Test void disabledKeepsConventionalQuotation() {
        when(localities.findAllWithActiveFare()).thenReturn(List.of(Locality.builder().name("Morteros").build()));
        when(pricing.calculateTripPrice("Morteros", true, 1)).thenReturn(BigDecimal.TEN);
        new AskLocalityHandler(sessions, localities, pricing, messaging).handle(session, message("1"));
        assertThat(session.getCurrentStep()).isEqualTo("ASK_MARKETING_CONFIRMATION");
        verify(pricing).calculateTripPrice("Morteros", true, 1);
    }

    @Test void cordobaChoiceUsesConventionalQuotationWithoutLooping() {
        when(localities.findAllWithActiveFare()).thenReturn(List.of(Locality.builder().name("Morteros").build()));
        when(catalog.origins()).thenReturn(List.of("Morteros"));
        when(pricing.calculateTripPrice("Morteros", true, 1)).thenReturn(BigDecimal.TEN);
        session.setPickupLocality("Morteros");
        session.setCurrentStep("ASK_INTERURBAN_DESTINATION");
        var origin = new AskLocalityHandler(sessions, localities, pricing, messaging, Optional.of(catalog));
        new AskInterurbanDestinationHandler(catalog, sessions, messaging, localities, origin)
                .handle(session, message("c"));
        assertThat(session.getCurrentStep()).isEqualTo("ASK_MARKETING_CONFIRMATION");
        verify(pricing).calculateTripPrice("Morteros", true, 1);
        verify(catalog, never()).destinations(anyString());
    }

    @Test void invalidDestinationDoesNotAdvanceSession() {
        session.setPickupLocality("Morteros");
        session.setCurrentStep("ASK_INTERURBAN_DESTINATION");
        var handler = new AskInterurbanDestinationHandler(catalog, sessions, messaging, localities, mock(AskLocalityHandler.class));
        handler.handle(session, message("i_2"));
        verifyNoInteractions(sessions);
        assertThat(session.getDestination()).isNull();
    }
}
