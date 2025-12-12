
package org.example.apkahotels.repositories;

import org.example.apkahotels.models.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.initialization-mode=never",
        "spring.sql.init.mode=never"
})
class HotelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    @BeforeEach
    void setUp() {
        // Wyczyść bazę przed każdym testem
        hotelRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    void shouldFindHotelById() {
        // given
        Hotel hotel = new Hotel("Grand Hotel", "Kraków", "Luksusowy hotel", "5");
        Long savedId = (Long) entityManager.persistAndGetId(hotel);
        entityManager.flush();

        // when
        Optional<Hotel> found = hotelRepository.findById(savedId);

        // then
        assertTrue(found.isPresent());
        assertEquals("Grand Hotel", found.get().getName());
        assertEquals("Kraków", found.get().getCity());
    }

    @Test
    void shouldFindAllHotels() {
        // given
        Hotel hotel1 = new Hotel("Hotel A", "Kraków", "Opis A", "4");
        Hotel hotel2 = new Hotel("Hotel B", "Warszawa", "Opis B", "5");

        entityManager.persist(hotel1);
        entityManager.persist(hotel2);
        entityManager.flush();

        // when
        List<Hotel> hotels = hotelRepository.findAll();

        // then
        assertEquals(2, hotels.size());
    }

    @Test
    void shouldSearchHotelsByNameOrCity() {
        // given
        Hotel hotel1 = new Hotel("Grand Hotel", "Kraków", "Luksusowy hotel", "5");
        Hotel hotel2 = new Hotel("Budget Inn", "Warszawa", "Tani hotel", "3");
        Hotel hotel3 = new Hotel("City Hotel", "Kraków", "Hotel w centrum", "4");

        entityManager.persist(hotel1);
        entityManager.persist(hotel2);
        entityManager.persist(hotel3);
        entityManager.flush();

        // when
        List<Hotel> foundByCity = hotelRepository
                .findByNameContainingIgnoreCaseOrCityContainingIgnoreCase("kraków", "kraków");
        List<Hotel> foundByName = hotelRepository
                .findByNameContainingIgnoreCaseOrCityContainingIgnoreCase("grand", "grand");

        // then
        assertEquals(2, foundByCity.size());
        assertEquals(1, foundByName.size());
        assertEquals("Grand Hotel", foundByName.get(0).getName());
    }

    @Test
    void shouldSearchByKeyword() {
        // given
        Hotel hotel1 = new Hotel("Grand Hotel", "Kraków", "Luksusowy hotel", "5");
        Hotel hotel2 = new Hotel("Budget Inn", "Warszawa", "Tani hotel", "3");

        entityManager.persist(hotel1);
        entityManager.persist(hotel2);
        entityManager.flush();

        // when
        List<Hotel> found = hotelRepository.searchByKeyword("grand");

        // then
        assertEquals(1, found.size());
        assertEquals("Grand Hotel", found.get(0).getName());
    }
}