package org.example.apkahotels.services;

import org.example.apkahotels.models.Hotel;
import org.example.apkahotels.repositories.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private HotelService hotelService;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Kraków");
        testHotel.setStars("4");
        testHotel.setDescription("Piękny hotel testowy");
    }

    @Test
    void shouldGetAllHotels() {
        // given
        List<Hotel> expectedHotels = Arrays.asList(testHotel);
        when(hotelRepository.findAll()).thenReturn(expectedHotels);

        // when
        List<Hotel> actualHotels = hotelService.getAllHotels();

        // then
        assertEquals(1, actualHotels.size());
        assertEquals("Test Hotel", actualHotels.get(0).getName());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void shouldGetHotelById() {
        // given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));

        // when
        Hotel actualHotel = hotelService.getHotelById(1L);

        // then
        assertNotNull(actualHotel);
        assertEquals("Test Hotel", actualHotel.getName());
        assertEquals("Kraków", actualHotel.getCity());
    }

    @Test
    void shouldThrowExceptionWhenHotelNotFound() {
        // given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> hotelService.getHotelById(999L));

        assertTrue(exception.getMessage().contains("Hotel not found") ||
                exception.getMessage().contains("not found"));
    }

    @Test
    void shouldSaveHotel() {
        // given
        Hotel newHotel = new Hotel("New Hotel", "Warszawa", "Opis", "5");
        when(hotelRepository.save(any(Hotel.class))).thenReturn(newHotel);

        // when
        Hotel savedHotel = hotelService.saveHotel(newHotel);

        // then
        assertNotNull(savedHotel);
        assertEquals("New Hotel", savedHotel.getName());
        verify(hotelRepository).save(newHotel);
    }
}