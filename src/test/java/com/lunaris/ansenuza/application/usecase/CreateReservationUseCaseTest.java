          
                                                                                                                                                          
    package com.lunaris.ansenuza.application.usecase;                                                                                                     
                                                                                                                                                          
    import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;                                                              
                                                                                                                                                          
    class CreateReservationUseCaseTest {                                                                                                                  

        @Test
        void createsPassengerFromFullNameAndMapsOriginAliasForFareCalculation() throws Exception {
            PassengerRepository passengerRepository = mock(PassengerRepository.class);
            ReservationRepository reservationRepository = mock(ReservationRepository.class);
            FareRepository fareRepository = mock(FareRepository.class);
            when(passengerRepository.findByPhone("5493511111111")).thenReturn(Optional.empty());
            when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(fareRepository.findByLocalityNameIgnoreCase("Ansenuza")).thenReturn(Optional.of(
                    Fare.builder().localityName("Ansenuza").amount(new BigDecimal("96000")).build()));
            when(reservationRepository.countSequenceByRouteAndDate(any(), any(), any())).thenReturn(0L);
            when(reservationRepository.existsByReservationCode(any())).thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PricingAndScheduleService pricingService = new PricingAndScheduleService(
                    fareRepository, mock(LocalityRepository.class),
                    mock(BusinessParameterRepository.class), reservationRepository);
            CreateReservationUseCase useCase = new CreateReservationUseCase(
                    new ReservationService(reservationRepository, mock(ReservationEventRepository.class),
                            passengerRepository, mock(com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase.class)),
                    passengerRepository, pricingService, mock(SameDayBookingPolicy.class));
            String json = """
                    {"fullName":"juna fenogloi","phone":"3511111111","documentId":"30111222",
                     "date":"2026-08-01","origin":"Ansenuza","pickupAddress":"Belgrano 10",
                     "destination":"Córdoba","scheduleBlock":"08:00 AM","seats":1,"roundTrip":false}
                    """;
            CreateReservationRequest request = new com.fasterxml.jackson.databind.ObjectMapper()
                    .findAndRegisterModules().readValue(json, CreateReservationRequest.class);

            Reservation result = useCase.execute(request);

            assertEquals("juna", result.getPassenger().getFirstName());
            assertEquals("fenogloi", result.getPassenger().getLastName());
            assertEquals("Ansenuza", result.getPickupLocality());
            assertEquals("30111222", result.getPassenger().getCuil());
            assertEquals("Belgrano 10", result.getPassenger().getAddress());
            assertEquals("08:00 AM", result.getDepartureSchedule());
            assertEquals("ANS-COR-001", result.getReservationCode());
            assertEquals(new BigDecimal("56000.00"), result.getAmount());
        }
                                                                                                                                                          
        @Test                                                                                                                                             
        void executeDelegatesAmountCalculationToPricingService() {                                                                                        
            UUID passengerId = UUID.randomUUID();                                                                                                         
            Passenger passenger = Passenger.builder()                                                                                                     
                    .id(passengerId)                                                                                                                      
                    .firstName("Juan")                                                                                                                    
                    .lastName("Perez")                                                                                                                    
                    .phone("1234567890")                                                                                                                  
                    .build();                                                                                                                             
                                                                                                                                                          
            PassengerRepository passengerRepository = mock(PassengerRepository.class);                                                                    
            when(passengerRepository.findById(passengerId)).thenReturn(Optional.of(passenger));                                                           
            when(passengerRepository.save(any(Passenger.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
                                                                                                                                                          
            FareRepository fareRepository = mock(FareRepository.class);                                                                                   
            when(fareRepository.findByLocalityNameIgnoreCase("Morteros")).thenReturn(Optional.of(                                                         
                    Fare.builder()                                                                                                                        
                            .localityName("Morteros")                                                                                                     
                            .amount(new BigDecimal("100000"))                                                                                             
                            .build()                                                                                                                      
            ));                                                                                                                                           
                                                                                                                                                          
            LocalityRepository localityRepository = mock(LocalityRepository.class);                                                                       
            BusinessParameterRepository businessParameterRepository = mock(BusinessParameterRepository.class);                                            
            ReservationRepository reservationRepository = mock(ReservationRepository.class);                                                              
                                                                                                                                                          
            PricingAndScheduleService pricingService = new PricingAndScheduleService(                                                                     
                    fareRepository,                                                                                                                       
                    localityRepository,                                                                                                                   
                    businessParameterRepository,                                                                                                          
                    reservationRepository                                                                                                                 
            );                                                                                                                                            
                                                                                                                                                          
            ReservationEventRepository reservationEventRepository = mock(ReservationEventRepository.class);                                               
            ReservationService reservationService = new ReservationService(                                                                               
                    reservationRepository,                                                                                                                
                    reservationEventRepository,                                                                                                           
                    passengerRepository,
                    mock(com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase.class)
            );                                                                                                                                            
                                                                                                                                                          
            when(reservationRepository.countSequenceByRouteAndDate(any(), any(), any())).thenReturn(0L);                                                  
            when(reservationRepository.existsByReservationCode(any())).thenReturn(false);                                                                 
                                                                                                                                                          
            ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);                                                   
            when(reservationRepository.save(reservationCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));                            
                                                                                                                                                          
            CreateReservationUseCase useCase = new CreateReservationUseCase(                                                                              
                    reservationService,                                                                                                                   
                    passengerRepository,                                                                                                                  
                    pricingService,
                    mock(SameDayBookingPolicy.class)
            );                                                                                                                                            
                                                                                                                                                          
            CreateReservationRequest request = new CreateReservationRequest(                                                                              
                    passengerId,                                                                                                                          
                    LocalDate.of(2026, 6, 30),                                                                                                            
                    "Morteros",                                                                                                                           
                    "Av. San Martín 123",                                                                                                                 
                    "Córdoba",                                                                                                                            
                    false,                                                                                                                                
                    null,                                                                                                                                 
                    false,                                                                                                                                
                    "nota",                                                                                                                               
                    2,                                                                                                                                    
                    "Ana, Luis",
                    null
            );                                                                                                                                            
                                                                                                                                                          
            Reservation result = useCase.execute(request);                                                                                                
            Reservation persisted = reservationCaptor.getValue();                                                                                         
                                                                                                                                                          
            assertSame(persisted, result);                                                                                                                
            assertEquals(new BigDecimal("116000.00"), persisted.getAmount());                                                                             
            assertEquals(2, persisted.getPassengerCount());                                                                                               
            assertEquals("PENDING_PAYMENT", persisted.getStatus());
            assertEquals("MOR-COR-001", persisted.getReservationCode());
            assertEquals(ReservationSource.WEB, persisted.getSource());
            assertSame(passenger, persisted.getPassenger());                                                                                              
        }                                                                                                                                                 
    }
