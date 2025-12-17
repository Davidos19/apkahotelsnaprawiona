package org.example.apkahotels.controllers;

import jakarta.validation.Valid;
import org.example.apkahotels.dto.RoomTypeStatsDTO;
import org.example.apkahotels.models.Hotel;
import org.example.apkahotels.models.Reservation;
import org.example.apkahotels.models.Review;
import org.example.apkahotels.models.Room;
import org.example.apkahotels.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.apkahotels.services.SearchService;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;



@Controller
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final UserService userService;


    public WebController(HotelService hotelService, ReservationService reservationService,
                         ReviewService reviewService, RoomService roomService, UserService userService) {
        this.hotelService = hotelService;
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        this.roomService = roomService;
        this.userService = userService;

    }
    @Autowired
    private SearchService searchService;

    // ✅ NOWY ENDPOINT WYSZUKIWANIA
    @GetMapping("/hotels/search")
    public String searchHotels(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String checkIn,
                               @RequestParam(required = false) String checkOut,
                               @RequestParam(required = false) Double minRating,
                               @RequestParam(required = false) BigDecimal minPrice,
                               @RequestParam(required = false) BigDecimal maxPrice,
                               @RequestParam(required = false, defaultValue = "name") String sortBy,
                               Model model) {

        SearchService.SearchCriteria criteria = new SearchService.SearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setLocation(location);
        criteria.setMinRating(minRating); // ✅ TERAZ DZIAŁA
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setSortBy(sortBy);

        // Parse daty
        if (checkIn != null && !checkIn.isEmpty()) {
            try {
                criteria.setCheckIn(LocalDate.parse(checkIn));
            } catch (Exception e) {
                // Ignoruj błędną datę
            }
        }
        if (checkOut != null && !checkOut.isEmpty()) {
            try {
                criteria.setCheckOut(LocalDate.parse(checkOut));
            } catch (Exception e) {
                // Ignoruj błędną datę
            }
        }

        List<Hotel> hotels = searchService.searchHotels(criteria);

        model.addAttribute("hotels", hotels);
        model.addAttribute("searchCriteria", criteria);
        model.addAttribute("popularDestinations", searchService.getPopularDestinations());
        model.addAttribute("bestDeals", searchService.getBestDeals());

        return "hotels"; // Lub "search_results" jeśli utworzysz nowy template
    }

    // ✅ AUTOCOMPLETE API
    @GetMapping("/api/locations/suggest")
    @ResponseBody
    public List<String> suggestLocations(@RequestParam String query) {
        return searchService.getLocationSuggestions(query);
    }


    @GetMapping("/")
    public String showHomePage(Model model,
                               @RequestParam(required = false) String checkIn,
                               @RequestParam(required = false) String checkOut) {

        LocalDate checkInDate = parseDate(checkIn);
        LocalDate checkOutDate = parseDate(checkOut);

        // Pobierz hotele z dostępnością dla podanych dat
        List<Hotel> hotels;
        if (checkInDate != null && checkOutDate != null) {
            hotels = hotelService.getAllHotelsWithAvailability(checkInDate, checkOutDate);
        } else {
            // Jeśli nie ma dat, pokaż wszystkie hotele
            hotels = hotelService.getAllHotels();
        }

        // ✅ DODAJ POLECANE HOTELE (pierwsze 6 dla sekcji "Popularne hotele")
        List<Hotel> featuredHotels = hotels.stream()
                .limit(6)
                .collect(Collectors.toList());

        model.addAttribute("hotels", hotels);
        model.addAttribute("featuredHotels", featuredHotels); // ✅ DODANE
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);

        // Dodaj rezerwacje użytkownika
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(username)) {
            List<Reservation> userReservations = reservationService.getReservationsByUsername(username);
            model.addAttribute("reservations", userReservations);

            // ✅ DODAJ STATYSTYKI UŻYTKOWNIKA
            model.addAttribute("userStats", reservationService.getReservationStats(username));
            model.addAttribute("activeReservations", reservationService.getActiveReservationsByUsername(username));
        }

        // ✅ DODAJ OGÓLNE STATYSTYKI DLA STRONY GŁÓWNEJ
        try {
            model.addAttribute("totalHotels", hotels.size());
            model.addAttribute("totalReservations", reservationService.getTotalReservations());

            // Statystyki dla anonimowych użytkowników
            model.addAttribute("totalClients", userService.getAllUsers().size());

        } catch (Exception e) {
            logger.error("Błąd przy ładowaniu statystyk: {}", e.getMessage());
            // Wartości domyślne
            model.addAttribute("totalHotels", 0);
            model.addAttribute("totalReservations", 0);
            model.addAttribute("totalClients", 0);
        }

        return "index";
    }

    // ===== POPRAWIONY CONTROLLER DLA SZCZEGÓŁÓW HOTELU =====
    @GetMapping({"/hotel/{id}", "/hotels/{id}"})
    public String hotelDetails(@PathVariable Long id,
                               @RequestParam(required = false) String checkIn,
                               @RequestParam(required = false) String checkOut,
                               Model model) {

        logger.info("Dostęp do hotelu ID: {}", id);

        Hotel hotel = hotelService.getHotelById(id);
        if (hotel == null) {
            logger.warn("Hotel o ID {} nie istnieje", id);
            return "redirect:/";
        }

        LocalDate checkInDate = parseDate(checkIn);
        LocalDate checkOutDate = parseDate(checkOut);

        if (checkInDate == null || checkOutDate == null) {
            checkInDate = LocalDate.now();
            checkOutDate = LocalDate.now().plusDays(1);
        }

        try {
            // NAJPIERW SPRÓBUJ STARĄ METODĘ (która na pewno istnieje)
            List<RoomService.RoomTypeAvailability> roomTypesAvailability = roomService.getRoomTypesWithAvailability(id, checkInDate, checkOutDate);

            // Policz całkowitą dostępność
            int totalAvailable = roomTypesAvailability.stream()
                    .mapToInt(RoomService.RoomTypeAvailability::getAvailableCount)
                    .sum();

            hotel.setAvailableRooms(totalAvailable);

            // WAŻNE: Użyj tej samej nazwy co w HTML
            model.addAttribute("roomTypesWithAvailability", roomTypesAvailability);
            model.addAttribute("checkIn", checkInDate);
            model.addAttribute("checkOut", checkOutDate);

            logger.debug("Załadowano {} typów pokoi, {} dostępnych łącznie", roomTypesAvailability.size(), totalAvailable);

        } catch (Exception e) {
            logger.error("Błąd podczas ładowania pokoi dla hotelu {}: {}", id, e.getMessage(), e);
            model.addAttribute("roomTypesWithAvailability", new ArrayList<>());
            model.addAttribute("error", "Błąd podczas ładowania pokoi");
            hotel.setAvailableRooms(0);
        }

        // Pobierz recenzje
        try {
            model.addAttribute("reviews", reviewService.getReviewsForHotel(id));
            model.addAttribute("averageRating", reviewService.getAverageRating(id));
        } catch (Exception e) {
            logger.warn("Błąd podczas pobierania recenzji: {}", e.getMessage());
            model.addAttribute("reviews", new ArrayList<>());
            model.addAttribute("averageRating", 0.0);
        }

        model.addAttribute("hotel", hotel);
        model.addAttribute("reservation", new Reservation());

        return "hotel_details";
    }


    @PostMapping("/reservation")
    public String makeReservation(@ModelAttribute Reservation reservation,
                                  @RequestParam(required = false) Long hotelId,
                                  @RequestParam(required = false) String roomType,
                                  @RequestParam(required = false) Integer capacity,
                                  @RequestParam(required = false) String checkIn,
                                  @RequestParam(required = false) String checkOut,
                                  RedirectAttributes redirectAttributes) {

        try {
            // Walidacja danych wejściowych
            validateReservationInput(hotelId, roomType, capacity, checkIn, checkOut);

            // Parsuj daty
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            // UŻYJ ZOPTYMALIZOWANEJ METODY
            Room assignedRoom = roomService.assignAvailableRoomOfTypeOptimized(hotelId, roomType, capacity, checkInDate, checkOutDate);

            // Skonfiguruj rezerwację
            reservation.setHotelId(hotelId);
            reservation.setRoomId(assignedRoom.getId());
            reservation.setCheckIn(checkInDate);
            reservation.setCheckOut(checkOutDate);
            reservation.setUsername(getCurrentUsername());

            // Zapisz rezerwację
            reservationService.makeReservation(reservation);

            redirectAttributes.addFlashAttribute("message",
                    String.format("Rezerwacja pokoju %s (%s) została utworzona! Koszt: %s zł",
                            assignedRoom.getRoomNumber(), roomType + " " + capacity + " os.",
                            reservation.getTotalPrice()));

            return "redirect:/my-reservations";

        } catch (Exception e) {
            logger.error("Błąd rezerwacji: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Błąd: " + e.getMessage());
            return "redirect:/hotels/" + hotelId;
        }
    }

    // Metody pomocnicze
    private void validateReservationInput(Long hotelId, String roomType, Integer capacity, String checkIn, String checkOut) {
        if (hotelId == null) throw new RuntimeException("Hotel nie został wybrany");
        if (roomType == null || roomType.trim().isEmpty()) throw new RuntimeException("Typ pokoju nie został wybrany");
        if (capacity == null) throw new RuntimeException("Pojemność pokoju nie została określona");
        if (checkIn == null || checkIn.trim().isEmpty()) throw new RuntimeException("Data przyjazdu jest wymagana");
        if (checkOut == null || checkOut.trim().isEmpty()) throw new RuntimeException("Data wyjazdu jest wymagana");
    }

    private String getCurrentUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new RuntimeException("Musisz być zalogowany aby dokonać rezerwacji");
        }
        return username;
    }
    @PostMapping("/cancelReservation")
    public String cancelReservation(@RequestParam("reservationId") Long reservationId,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(reservationId);
            redirectAttributes.addFlashAttribute("message", "Rezerwacja anulowana!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Błąd przy anulowaniu rezerwacji: " + e.getMessage());
        }
        return "redirect:/";
    }



    @PostMapping("/reservation/update")
    public String updateReservation(@ModelAttribute("reservation") Reservation updatedRes) {
        reservationService.updateReservation(updatedRes);
        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchHotels(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("hotels", reservationService.searchHotels(keyword));
        model.addAttribute("reservations", reservationService.getAllReservations());
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("keyword", keyword);
        return "index";
    }



    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            logger.warn("Nie można sparsować daty: {}", dateStr);
            return null;
        }
    }

    @GetMapping("/hotels/{id}/rooms")
    @ResponseBody
    public List<Map<String, Object>> getAvailableRoomsForDates(
            @PathVariable Long id,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {

        try {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            // Pobierz wszystkie pokoje hotelu
            List<Room> allRooms = roomService.getRoomsByHotelId(id);

            // Grupuj pokoje według typu
            Map<String, List<Room>> roomsByType = allRooms.stream()
                    .collect(Collectors.groupingBy(Room::getRoomType));

            List<Map<String, Object>> result = new ArrayList<>();

            for (Map.Entry<String, List<Room>> entry : roomsByType.entrySet()) {
                String roomType = entry.getKey();
                List<Room> roomsOfType = entry.getValue();

                // Sprawdź dostępność dla tego typu pokoju
                List<Room> availableRooms = roomService.getAvailableRoomsByTypeAndDates(
                        id, roomType, roomsOfType.get(0).getCapacity(), checkInDate, checkOutDate);

                if (!availableRooms.isEmpty()) {
                    Room sampleRoom = roomsOfType.get(0);

                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("type", roomType);
                    roomInfo.put("capacity", sampleRoom.getCapacity());
                    roomInfo.put("pricePerNight", sampleRoom.getPrice());
                    roomInfo.put("availableCount", availableRooms.size());
                    roomInfo.put("features", "WiFi, Klimatyzacja, TV"); // Domyślne udogodnienia

                    // ✅ POPRAWIONE - bez .doubleValue()
                    long nights = checkInDate.until(checkOutDate).getDays();
                    double totalPrice = sampleRoom.getPrice() * nights; // usunięte .doubleValue()
                    roomInfo.put("totalPrice", totalPrice);
                    roomInfo.put("nights", nights);

                    result.add(roomInfo);
                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }


}
