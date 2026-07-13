 package com.lunaris.ansenuza.domain.model.service;                                                                                                    
                                                                                                                                                          
    import static org.junit.jupiter.api.Assertions.assertEquals;                                                                                          
    import static org.mockito.Mockito.*;                                                                                                                  
                                                                                                                                                          
    import java.math.BigDecimal;                                                                                                                          
    import java.util.Optional;
    import org.junit.jupiter.api.Test;
    import com.lunaris.ansenuza.domain.model.Fare;
    import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
    import com.lunaris.ansenuza.domain.repository.FareRepository;
    import com.lunaris.ansenuza.domain.repository.LocalityRepository;
    import com.lunaris.ansenuza.domain.repository.ReservationRepository;
  
    class PricingAndScheduleServiceTest {
  
        @Test
        void calculateTripPriceAppliesOneWayRuleAndSeatCount() {
            PricingAndScheduleService service = newService();
  
            BigDecimal amount = service.calculateTripPrice("Morteros", false, 2);
  
            assertEquals(new BigDecimal("116000.00"), amount);
        }
  
        @Test
        void calculateReservationAmountUsesZoneLocalityAndSamePricingRule() {
            PricingAndScheduleService service = newService();
  
            BigDecimal amount = service.calculateReservationAmount("Córdoba", "Morteros", false, 1);
  
            assertEquals(new BigDecimal("58000.00"), amount);
        }
  
        private PricingAndScheduleService newService() {
            FareRepository fareRepository = mock(FareRepository.class);
            LocalityRepository localityRepository = mock(LocalityRepository.class);
            BusinessParameterRepository businessParameterRepository = mock(BusinessParameterRepository.class);
            ReservationRepository reservationRepository = mock(ReservationRepository.class);
  
            Fare fare = Fare.builder()
                    .localityName("Morteros")
                    .amount(new BigDecimal("100000"))
                    .build();
  
            when(fareRepository.findByLocalityNameIgnoreCase("Morteros")).thenReturn(Optional.of(fare));
  
            return new PricingAndScheduleService(
                    fareRepository,
                    localityRepository,
                    businessParameterRepository,
                    reservationRepository
            );
        }
    }
