
package org.example.apkahotels.services;

import org.example.apkahotels.models.Hotel;
import org.example.apkahotels.models.Reservation;
import org.example.apkahotels.models.Room;
import org.example.apkahotels.repositories.HotelRepository;
import org.example.apkahotels.repositories.ReservationRepository;
import org.example.apkahotels.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoomService roomService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationService reservationService;

    private Hotel testHotel;
    private Room testRoom;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);

        // Test hotel
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Kraków");

        // Test room - używamy double dla price
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setHotelId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Standard");
        testRoom.setCapacity(2);
        testRoom.setPrice(200.0); // double

        // Test reservation
        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setHotelId(1L);
        testReservation.setRoomId(1L);
        testReservation.setUsername("testuser");
        testReservation.setCheckIn(LocalDate.now().plusDays(1));
        testReservation.setCheckOut(LocalDate.now().plusDays(3));
        testReservation.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateReservation() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);

        when(roomService.getRoomById(1L)).thenReturn(testRoom);
        when(roomService.getAvailableRoomsByTypeAndDates(anyLong(), anyString(), anyInt(), any(), any()))
                .thenReturn(Arrays.asList(testRoom));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation newReservation = new Reservation();
        newReservation.setHotelId(1L);
        newReservation.setRoomId(1L);
        newReservation.setCheckIn(LocalDate.now().plusDays(1));
        newReservation.setCheckOut(LocalDate.now().plusDays(3));

        // when
        reservationService.makeReservation(newReservation);

        // then
        verify(reservationRepository).save(any(Reservation.class));
        assertEquals("testuser", newReservation.getUsername());
        assertNotNull(newReservation.getCreatedAt());
        assertEquals(newReservation.getCheckIn(), newReservation.getStartDate());
        assertEquals(newReservation.getCheckOut(), newReservation.getEndDate());
    }

    @Test
    void shouldCompleteBookingFlow() {
        // given - symulacja pełnego procesu rezerwacji
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);

        // 1. Sprawdź dostępność pokoju
        when(roomService.getRoomById(1L)).thenReturn(testRoom);
        when(roomService.getAvailableRoomsByTypeAndDates(anyLong(), anyString(), anyInt(), any(), any()))
                .thenReturn(Arrays.asList(testRoom));

        // 2. Zapisz rezerwację
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // 3. Pobierz zapisaną rezerwację
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        Reservation newReservation = new Reservation();
        newReservation.setHotelId(1L);
        newReservation.setRoomId(1L);
        newReservation.setCheckIn(LocalDate.now().plusDays(1));
        newReservation.setCheckOut(LocalDate.now().plusDays(3));

        // when - wykonaj pełny flow
        // 1. Utwórz rezerwację
        reservationService.makeReservation(newReservation);

        // 2. Sprawdź czy została zapisana
        Reservation savedReservation = reservationService.getReservationById(1L);

        // then
        assertNotNull(savedReservation);
        assertEquals(1L, savedReservation.getId());
        assertEquals("testuser", savedReservation.getUsername());
        assertEquals(BigDecimal.valueOf(400.0), savedReservation.getTotalPrice());

        verify(roomService).getRoomById(1L);
        verify(roomService).getAvailableRoomsByTypeAndDates(1L, "Standard", 2,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationRepository).findById(1L);
    }

    @Test
    void shouldFailReservationWithInvalidDates() {
        // given
        Reservation invalidReservation = new Reservation();
        invalidReservation.setCheckIn(LocalDate.now().plusDays(3));
        invalidReservation.setCheckOut(LocalDate.now().plusDays(1)); // Data wyjazdu przed przyjazdem

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(invalidReservation));

        assertTrue(exception.getMessage().contains("Data wyjazdu musi być po dacie przyjazdu"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldFailReservationWithPastDate() {
        // given
        Reservation pastReservation = new Reservation();
        pastReservation.setCheckIn(LocalDate.now().minusDays(1)); // Przeszła data
        pastReservation.setCheckOut(LocalDate.now().plusDays(1));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(pastReservation));

        assertTrue(exception.getMessage().contains("Data przyjazdu nie może być w przeszłości"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldFailReservationWithNoAvailableRooms() {
        // given
        when(roomService.getRoomById(1L)).thenReturn(testRoom);
        when(roomService.getAvailableRoomsByTypeAndDates(anyLong(), anyString(), anyInt(), any(), any()))
                .thenReturn(Collections.emptyList()); // Brak dostępnych pokoi

        Reservation newReservation = new Reservation();
        newReservation.setHotelId(1L);
        newReservation.setRoomId(1L);
        newReservation.setCheckIn(LocalDate.now().plusDays(1));
        newReservation.setCheckOut(LocalDate.now().plusDays(3));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(newReservation));

        assertTrue(exception.getMessage().contains("Brak dostępnych pokoi tego typu"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldCancelReservation() {
        // given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // when
        reservationService.cancelReservation(1L);

        // then
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonexistentReservation() {
        // given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(999L));

        assertTrue(exception.getMessage().contains("Rezerwacja nie została znaleziona"));
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldCheckRoomAvailability() {
        // given
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Long roomId = 1L;

        // Brak konfliktowych rezerwacji
        when(reservationRepository.findConflictingReservations(roomId, checkIn, checkOut))
                .thenReturn(Collections.emptyList());

        // when
        boolean isAvailable = reservationService.isRoomAvailable(roomId, checkIn, checkOut, null);

        // then
        assertTrue(isAvailable);
        verify(reservationRepository).findConflictingReservations(roomId, checkIn, checkOut);
    }

    @Test
    void shouldDetectRoomUnavailability() {
        // given
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Long roomId = 1L;

        // Konfliktowa rezerwacja
        when(reservationRepository.findConflictingReservations(roomId, checkIn, checkOut))
                .thenReturn(Arrays.asList(testReservation));

        // when
        boolean isAvailable = reservationService.isRoomAvailable(roomId, checkIn, checkOut, null);

        // then
        assertFalse(isAvailable);
    }

    @Test
    void shouldCalculateTotalPrice() {
        // given
        when(roomService.getRoomById(1L)).thenReturn(testRoom);
        when(roomService.getAvailableRoomsByTypeAndDates(anyLong(), anyString(), anyInt(), any(), any()))
                .thenReturn(Arrays.asList(testRoom));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);

        Reservation newReservation = new Reservation();
        newReservation.setHotelId(1L);
        newReservation.setRoomId(1L);
        newReservation.setCheckIn(LocalDate.now().plusDays(1));
        newReservation.setCheckOut(LocalDate.now().plusDays(4)); // 3 noce

        // when
        reservationService.makeReservation(newReservation);

        // then - ✅ POPRAWIONY TEST - używamy ArgumentCaptor
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());

        Reservation capturedReservation = reservationCaptor.getValue();
        assertEquals(BigDecimal.valueOf(600.0), capturedReservation.getTotalPrice());
    }
}