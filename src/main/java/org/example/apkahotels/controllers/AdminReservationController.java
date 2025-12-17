package org.example.apkahotels.controllers;

import org.example.apkahotels.models.*;
import org.example.apkahotels.services.*;
import org.example.apkahotels.dto.RoomTypeStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reservations")
@PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER', 'RECEPTIONIST')")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final UserService userService;

    public AdminReservationController(ReservationService reservationService,
                                      HotelService hotelService,
                                      RoomService roomService,
                                      UserService userService) {
        this.reservationService = reservationService;
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.userService = userService;
    }

    @GetMapping
    public String listAllReservations(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(defaultValue = "checkIn") String sortBy,
                                      @RequestParam(defaultValue = "desc") String sortDir,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) Long hotelId,
                                      @RequestParam(required = false) String username,
                                      Model model) {

        try {
            // Utworz obiekt do sortowania
            Sort sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Pobierz rezerwacje z filtrowaniem
            Page<Reservation> reservationsPage = reservationService.getFilteredReservations(
                    pageable, status, hotelId, username);

            // Przygotuj dodatkowe dane
            Map<Long, Hotel> hotelDetails = new HashMap<>();
            Map<Long, Room> roomDetails = new HashMap<>();
            Map<String, AppUser> userDetails = new HashMap<>();

            for (Reservation reservation : reservationsPage.getContent()) {
                // Załaduj szczegóły hotelu
                if (reservation.getHotelId() != null && !hotelDetails.containsKey(reservation.getHotelId())) {
                    Hotel hotel = hotelService.getHotelById(reservation.getHotelId());
                    if (hotel != null) {
                        hotelDetails.put(reservation.getHotelId(), hotel);
                    }
                }

                // Załaduj szczegóły pokoju
                if (reservation.getRoomId() != null && !roomDetails.containsKey(reservation.getRoomId())) {
                    Room room = roomService.getRoomById(reservation.getRoomId());
                    if (room != null) {
                        roomDetails.put(reservation.getRoomId(), room);
                    }
                }

                // Załaduj szczegóły użytkownika
                if (reservation.getUsername() != null && !userDetails.containsKey(reservation.getUsername())) {
                    userService.findByUsername(reservation.getUsername())
                            .ifPresent(user -> userDetails.put(reservation.getUsername(), user));
                }
            }

            // ✅ OBLICZ STATYSTYKI W KONTROLERZE
            List<Reservation> allContentReservations = reservationsPage.getContent();
            long confirmedCount = allContentReservations.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()))
                    .count();
            long pendingCount = allContentReservations.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .count();
            long cancelledCount = allContentReservations.stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .count();

            // Dodaj dane do modelu
            model.addAttribute("reservations", reservationsPage);
            model.addAttribute("hotelDetails", hotelDetails);
            model.addAttribute("roomDetails", roomDetails);
            model.addAttribute("userDetails", userDetails);

            // ✅ STATYSTYKI
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("cancelledCount", cancelledCount);

            // Dodaj opcje filtrowania
            model.addAttribute("hotels", hotelService.getAllHotels());
            model.addAttribute("statusOptions", List.of("CONFIRMED", "CANCELLED", "PENDING", "COMPLETED"));

            // Parametry do zachowania stanu filtrów
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentHotelId", hotelId);
            model.addAttribute("currentUsername", username);
            model.addAttribute("currentSort", sortBy);
            model.addAttribute("currentSortDir", sortDir);

        } catch (Exception e) {
            model.addAttribute("error", "Błąd przy ładowaniu rezerwacji: " + e.getMessage());
            e.printStackTrace();
        }

        return "admin_reservations";
    }


    @GetMapping("/{id}")
    public String viewReservationDetails(@PathVariable Long id, Model model) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                model.addAttribute("error", "Rezerwacja nie została znaleziona");
                return "redirect:/admin/reservations";
            }

            // Załaduj szczegóły
            Hotel hotel = hotelService.getHotelById(reservation.getHotelId());
            Room room = roomService.getRoomById(reservation.getRoomId());
            AppUser user = userService.findByUsername(reservation.getUsername()).orElse(null);

            model.addAttribute("reservation", reservation);
            model.addAttribute("hotel", hotel);
            model.addAttribute("room", room);
            model.addAttribute("user", user);

            return "admin_reservation_details";

        } catch (Exception e) {
            model.addAttribute("error", "Błąd przy ładowaniu szczegółów rezerwacji: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }

    @GetMapping("/{id}/edit")
    public String editReservation(@PathVariable Long id, Model model) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return "redirect:/admin/reservations";
            }

            model.addAttribute("reservation", reservation);
            model.addAttribute("hotels", hotelService.getAllHotels());

            // ✅ ZAWSZE ZAŁADUJ POKOJE DLA AKTUALNEGO HOTELU
            if (reservation.getHotelId() != null) {
                List<Room> rooms = roomService.getRoomsByHotelId(reservation.getHotelId());
                model.addAttribute("rooms", rooms);

                // ✅ DODAJ AKTUALNY POKÓJ DO MODELU
                if (reservation.getRoomId() != null) {
                    Room currentRoom = roomService.getRoomById(reservation.getRoomId());
                    model.addAttribute("currentRoom", currentRoom);
                }
            }

            // ✅ DODAJ AKTUALNY HOTEL DO MODELU
            if (reservation.getHotelId() != null) {
                Hotel currentHotel = hotelService.getHotelById(reservation.getHotelId());
                model.addAttribute("currentHotel", currentHotel);
            }

            return "admin_edit_reservation";

        } catch (Exception e) {
            model.addAttribute("error", "Błąd przy ładowaniu rezerwacji: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }


    @PostMapping("/{id}/update")
    public String updateReservation(@PathVariable Long id,
                                    @RequestParam Map<String, String> allParams,
                                    RedirectAttributes redirectAttributes) {
        try {
            Reservation existingReservation = reservationService.getReservationById(id);
            if (existingReservation == null) {
                redirectAttributes.addFlashAttribute("error", "Rezerwacja nie została znaleziona");
                return "redirect:/admin/reservations";
            }

            // ✅ PRZETWÓRZ roomId PRZED MAPOWANIEM
            String roomIdParam = allParams.get("roomId");
            Long actualRoomId = null;

            if (roomIdParam != null && roomIdParam.startsWith("TYPE:")) {
                // To jest typ pokoju, znajdź dostępny pokój
                String roomTypeInfo = roomIdParam.replace("TYPE:", "");
                String[] parts = roomTypeInfo.split("_");
                String roomType = parts[0];
                Integer capacity = Integer.parseInt(parts[1]);

                LocalDate checkIn = LocalDate.parse(allParams.get("checkIn"));
                LocalDate checkOut = LocalDate.parse(allParams.get("checkOut"));
                Long hotelId = Long.parseLong(allParams.get("hotelId"));

                // Znajdź dostępny pokój tego typu
                List<Room> availableRooms = roomService.getAvailableRoomsByTypeAndDates(
                        hotelId, roomType, capacity, checkIn, checkOut
                );

                if (!availableRooms.isEmpty()) {
                    actualRoomId = availableRooms.get(0).getId();
                } else {
                    redirectAttributes.addFlashAttribute("error", "Brak dostępnych pokoi typu " + roomType);
                    return "redirect:/admin/reservations/" + id + "/edit";
                }
            } else if (roomIdParam != null && !roomIdParam.isEmpty()) {
                actualRoomId = Long.parseLong(roomIdParam);
            }

            // ✅ RĘCZNIE UTWÓRZ RESERVATION Z POPRAWNYMI WARTOŚCIAMI
            existingReservation.setStatus(allParams.get("status"));
            existingReservation.setCheckIn(LocalDate.parse(allParams.get("checkIn")));
            existingReservation.setCheckOut(LocalDate.parse(allParams.get("checkOut")));
            existingReservation.setHotelId(Long.parseLong(allParams.get("hotelId")));
            existingReservation.setRoomId(actualRoomId);

            // ✅ USTAW CENE - zmienione na Double
            String totalPriceParam = allParams.get("totalPrice");
            if (totalPriceParam != null && !totalPriceParam.isEmpty()) {
                existingReservation.setTotalPrice(Double.parseDouble(totalPriceParam));
            }

            // Ustaw daty dla kompatybilności
            existingReservation.setStartDate(existingReservation.getCheckIn());
            existingReservation.setEndDate(existingReservation.getCheckOut());

            reservationService.updateReservation(existingReservation);
            redirectAttributes.addFlashAttribute("message", "Rezerwacja została zaktualizowana");

            return "redirect:/admin/reservations";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Błąd: " + e.getMessage());
            return "redirect:/admin/reservations/" + id + "/edit";
        }
    }

    // API endpoint dla recepcjonistów
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER', 'RECEPTIONIST')")
    public String getHotelReservations(@PathVariable Long hotelId, Model model) {
        try {
            List<Reservation> reservations = reservationService.getReservationsByHotelId(hotelId);
            Hotel hotel = hotelService.getHotelById(hotelId);

            model.addAttribute("reservations", reservations);
            model.addAttribute("hotel", hotel);
            model.addAttribute("currentHotelId", hotelId);

            return "admin_hotel_reservations";

        } catch (Exception e) {
            model.addAttribute("error", "Błąd: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }

    @GetMapping("/search-users")
    @ResponseBody
    public List<Map<String, String>> searchUsers(@RequestParam String query) {
        try {
            List<AppUser> users = userService.getAllUsers().stream()
                    .filter(user ->
                            user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                                    (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(query.toLowerCase())) ||
                                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase()))
                    )
                    .limit(10)
                    .collect(Collectors.toList());

            return users.stream()
                    .map(user -> {
                        Map<String, String> result = new HashMap<>();
                        result.put("username", user.getUsername());
                        result.put("fullName", user.getFirstName() + " " + user.getLastName());
                        result.put("email", user.getEmail());
                        return result;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/rooms-by-hotel")
    @ResponseBody
    public List<Map<String, Object>> getRoomsByHotel(@RequestParam Long hotelId) {
        try {
            List<Room> rooms = roomService.getRoomsByHotelId(hotelId);

            return rooms.stream()
                    .map(room -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", room.getId());
                        result.put("roomNumber", room.getRoomNumber());
                        result.put("roomType", room.getRoomType());
                        result.put("pricePerNight", room.getPrice()); // używaj getPrice()
                        result.put("capacity", room.getCapacity());
                        return result;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ✅ NOWY ENDPOINT DLA TYPÓW POKOI
    @GetMapping("/room-types-by-hotel")
    @ResponseBody
    public List<RoomTypeStatsDTO> getRoomTypesByHotel(@RequestParam Long hotelId,
                                                      @RequestParam(required = false) String checkIn,
                                                      @RequestParam(required = false) String checkOut) {
        try {
            LocalDate checkInDate = null;
            LocalDate checkOutDate = null;

            // Parsuj daty jeśli są podane
            if (checkIn != null && !checkIn.isEmpty() && checkOut != null && !checkOut.isEmpty()) {
                try {
                    checkInDate = LocalDate.parse(checkIn);
                    checkOutDate = LocalDate.parse(checkOut);
                } catch (Exception e) {
                    System.err.println("Błąd parsowania dat: " + e.getMessage());
                    // Kontynuuj bez dat
                }
            }

            if (checkInDate != null && checkOutDate != null) {
                // Pobierz typy pokoi z dostępnością dla konkretnych dat
                return roomService.getRoomTypesWithAvailabilityOptimized(hotelId, checkInDate, checkOutDate);
            } else {
                // Pobierz podstawowe statystyki typów pokoi
                return roomService.getCachedRoomTypeStats(hotelId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

}