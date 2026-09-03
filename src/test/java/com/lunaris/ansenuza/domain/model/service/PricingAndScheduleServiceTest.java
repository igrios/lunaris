 package com.lunaris.ansenuza.domain.model.service;                                                                                                    
                                                                                                                                                          
    import static org.junit.jupiter.api.Assertions.assertEquals;                                                                                          
    import static org.mockito.Mockito.*;                                                                                                                  
                                                                                                                                                          
    import java.math.BigDecimal;                                                                                                                          
    import java.time.LocalDate;
    import java.util.Optional;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.params.ParameterizedTest;
    import org.junit.jupiter.params.provider.CsvSource;
    import com.lunaris.ansenuza.domain.model.Fare;
    import com.lunaris.ansenuza.domain.model.BusinessParameter;
    import com.lunaris.ansenuza.domain.model.Locality;
    import com.lunaris.ansenuza.domain.model.TripType;
    import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
    import com.lunaris.ansenuza.domain.repository.FareRepository;
    import com.lunaris.ansenuza.domain.repository.LocalityRepository;
    import com.lunaris.ansenuza.domain.repository.ReservationRepository;
  
    class PricingAndScheduleServiceTest {

        @Test
        void availableDepartureSchedulesUsesOutgoingDateAndFiltersFullBlocks() {
            ReservationRepository reservations = mock(ReservationRepository.class);
            LocalDate travelDate = LocalDate.of(2026, 8, 20);
            when(reservations.countReservedSeats(travelDate, "03:00 AM")).thenReturn(12L);
            when(reservations.countReservedSeats(travelDate, "08:00 AM")).thenReturn(4L);
            PricingAndScheduleService service = new PricingAndScheduleService(
                    mock(FareRepository.class), mock(LocalityRepository.class),
                    mock(BusinessParameterRepository.class), reservations);

            assertEquals(java.util.List.of("08:00 AM"),
                    service.availableDepartureSchedules("Morteros", "Córdoba", travelDate));
            verify(reservations).countReservedSeats(travelDate, "03:00 AM");
            verify(reservations).countReservedSeats(travelDate, "08:00 AM");
        }

        @Test
        void availableDepartureSchedulesReturnsDefaultBlocksWithoutExclusions() {
            ReservationRepository reservations = mock(ReservationRepository.class);
            PricingAndScheduleService service = new PricingAndScheduleService(
                    mock(FareRepository.class), mock(LocalityRepository.class),
                    mock(BusinessParameterRepository.class), reservations);

            assertEquals(java.util.List.of("03:00 AM", "08:00 AM"),
                    service.availableDepartureSchedules(
                            "Morteros", "Córdoba", LocalDate.of(2026, 8, 20)));
        }
  
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

        @Test
        void missingExplicitFareFallsBackToPricePerKm() {
            FareRepository fares = mock(FareRepository.class);
            LocalityRepository localities = mock(LocalityRepository.class);
            BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
            when(fares.findByLocalityNameIgnoreCase("Arrufó")).thenReturn(Optional.empty());
            when(localities.findFirstByNameIgnoreCase("Arrufó")).thenReturn(Optional.of(
                    Locality.builder().name("Arrufó").kmsToCordoba(300).build()));
            when(parameters.findByParameterKey("PRICE_PER_KM")).thenReturn(Optional.of(
                    BusinessParameter.builder()
                            .parameterKey("PRICE_PER_KM")
                            .parameterValue("1200")
                            .build()));
            PricingAndScheduleService service = new PricingAndScheduleService(
                    fares, localities, parameters, mock(ReservationRepository.class));

            assertEquals(new BigDecimal("360000"),
                    service.calculateTripPrice("Arrufó", true, 1));
        }

        @Test
        void missingFareAndKilometersUsesConfiguredDefaultFare() {
            FareRepository fares = mock(FareRepository.class);
            LocalityRepository localities = mock(LocalityRepository.class);
            BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
            when(fares.findByLocalityNameIgnoreCase("Nueva localidad"))
                    .thenReturn(Optional.empty());
            when(localities.findFirstByNameIgnoreCase("Nueva localidad"))
                    .thenReturn(Optional.empty());
            when(parameters.findByParameterKey("PRICE_PER_KM")).thenReturn(Optional.empty());
            when(parameters.findByParameterKey("DEFAULT_FARE")).thenReturn(Optional.of(
                    BusinessParameter.builder()
                            .parameterKey("DEFAULT_FARE")
                            .parameterValue("95000")
                            .build()));
            PricingAndScheduleService service = new PricingAndScheduleService(
                    fares, localities, parameters, mock(ReservationRepository.class));

            assertEquals(new BigDecimal("95000"),
                    service.calculateTripPrice("Nueva localidad", true, 1));
        }

        @Test
        void roundTripAndOpenReturnUseFullBaseFarePerPassenger() {
            PricingAndScheduleService service = newService();

            assertEquals(new BigDecimal("200000"),
                    service.calculateReservationAmount("Morteros", "Córdoba", TripType.ROUND_TRIP, 2));
            assertEquals(new BigDecimal("200000"),
                    service.calculateReservationAmount("Morteros", "Córdoba", TripType.OPEN_RETURN, 2));
        }

        @Test
        void oneWayUsesConfiguredExtraFeePerPassenger() {
            FareRepository fares = mock(FareRepository.class);
            BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
            when(fares.findByLocalityNameIgnoreCase("Morteros")).thenReturn(Optional.of(
                    Fare.builder().localityName("Morteros").amount(new BigDecimal("100000")).build()));
            when(parameters.findByParameterKey("ONE_WAY_EXTRA_AMOUNT")).thenReturn(Optional.of(
                    BusinessParameter.builder()
                            .parameterKey("ONE_WAY_EXTRA_AMOUNT")
                            .parameterValue("10000")
                            .build()));
            PricingAndScheduleService service = new PricingAndScheduleService(
                    fares, mock(LocalityRepository.class), parameters, mock(ReservationRepository.class));

            assertEquals(new BigDecimal("120000.00"),
                    service.calculateReservationAmount("Morteros", "Córdoba", TripType.ONE_WAY, 2));
        }

        @Test
        void oneWayExampleAppliesHalfBasePlusBusinessExtra() {
            FareRepository fares = mock(FareRepository.class);
            BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
            when(fares.findByLocalityNameIgnoreCase("Morteros")).thenReturn(Optional.of(
                    Fare.builder().localityName("Morteros")
                            .amount(new BigDecimal("90000")).build()));
            when(parameters.findByParameterKey("ONE_WAY_EXTRA_AMOUNT")).thenReturn(Optional.of(
                    BusinessParameter.builder()
                            .parameterKey("ONE_WAY_EXTRA_AMOUNT")
                            .parameterValue("7500")
                            .build()));
            PricingAndScheduleService service = new PricingAndScheduleService(
                    fares, mock(LocalityRepository.class), parameters,
                    mock(ReservationRepository.class));

            assertEquals(new BigDecimal("52500.00"),
                    service.calculateReservationAmount(
                            "Morteros", "Córdoba", TripType.ONE_WAY, 1));
        }

        @Test
        void cancellationSurchargeUsesConfiguredAmountPerPassengerAndCapsSeats() {
            BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
            when(parameters.findByParameterKey("ONE_WAY_EXTRA_AMOUNT")).thenReturn(Optional.of(
                    BusinessParameter.builder().parameterKey("ONE_WAY_EXTRA_AMOUNT")
                            .parameterValue("7500").build()));
            PricingAndScheduleService service = new PricingAndScheduleService(
                    mock(FareRepository.class), mock(LocalityRepository.class), parameters,
                    mock(ReservationRepository.class));

            assertEquals(new BigDecimal("22500"), service.calculateOneWaySurcharge(3));
            assertEquals(new BigDecimal("30000"), service.calculateOneWaySurcharge(99));
        }

        @ParameterizedTest
        @CsvSource({
                "San Guillermo, 07:20 hs",
                "Suardi, 07:40 hs",
                "Morteros, 08:00 hs",
                "Brinkmann, 08:20 hs",
                "Porteña, 08:40 hs",
                "Freyre, 09:00 hs",
                "La Paquita, 08:30 hs",
                "Altos de Chipión, 08:40 hs",
                "Balnearia, 09:00 hs",
                "Miramar, 09:10 hs"
        })
        void secondMorningScheduleUsesConfiguredDepartureTime(
                String locality, String expectedTime) {
            ReservationRepository reservations = mock(ReservationRepository.class);
            PricingAndScheduleService service = new PricingAndScheduleService(
                    mock(FareRepository.class),
                    mock(LocalityRepository.class),
                    mock(BusinessParameterRepository.class),
                    reservations);

            String result = service.calculateEstimatedPickupTime(
                    locality, "08:00", false, LocalDate.of(2026, 8, 10));

            assertEquals(expectedTime, result);
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
