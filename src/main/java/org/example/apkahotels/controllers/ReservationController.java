package org.example.apkahotels.controllers;

import org.example.apkahotels.models.Hotel;
import org.example.apkahotels.models.Reservation;
import org.example.apkahotels.models.Room;
import org.example.apkahotels.services.HotelService;
import org.example.apkahotels.services.ReservationService;
import org.example.apkahotels.services.RoomService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ✅ DODAJ IMPORT

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class ReservationController {
    private final ReservationService reservationService;
    private final HotelService hotelService;
    private final RoomService roomService;

    public ReservationController(ReservationService reservationService,
                                 HotelService hotelService,
                                 RoomService roomService) {
        this.reservationService = reservationService;
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @GetMapping("/api/hotels")
    public List<Hotel> getHotels() {
        return reservationService.getAllHotels();
    }

    @GetMapping("/api/reservations")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/api/cancelReservation/{id}")
    public String cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return "Rezerwacja została anulowana";
        } catch (Exception e) {
            return "Błąd: " + e.getMessage();
        }
    }

    @PostMapping("/api/reservations")
    public String makeReservation(@RequestBody Reservation reservation) {
        try {
            reservationService.makeReservation(reservation);
            return "Rezerwacja została utworzona";
        } catch (Exception e) {
            return "Błąd: " + e.getMessage();
        }
    }

    @GetMapping("/api/myReservations/filter")
    public String filterMyReservations(@RequestParam("date") String dateStr, Model model) {
        try {
            LocalDate filterDate = LocalDate.parse(dateStr);
            List<Reservation> filteredReservations = reservationService.getUserReservationsByDate(filterDate);
            model.addAttribute("reservations", filteredReservations);
            model.addAttribute("filterDate", filterDate);
            return "my_reservations";
        } catch (Exception e) {
            model.addAttribute("error", "Błąd filtrowania: " + e.getMessage());
            return "my_reservations";
        }
    }

    // ===== EDYCJA REZERWACJI DLA UŻYTKOWNIKÓW =====
    @GetMapping("/reservation/edit/{id}")
    public String editUserReservation(@PathVariable Long id, Model model) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                model.addAttribute("error", "Rezerwacja nie została znaleziona");
                return "redirect:/my-reservations";
            }

            // Sprawdź czy użytkownik może edytować tę rezerwację
            String currentUsername = getCurrentUsername(); // ✅ TERAZ BĘDZIE DZIAŁAĆ
            if (!reservation.getUsername().equals(currentUsername)) {
                model.addAttribute("error", "Nie masz uprawnień do edycji tej rezerwacji");
                return "redirect:/my-reservations";
            }

            // Sprawdź czy rezerwacja może być edytowana (np. nie jest anulowana)
            if ("CANCELLED".equals(reservation.getStatus())) {
                model.addAttribute("error", "Nie można edytować anulowanej rezerwacji");
                return "redirect:/my-reservations";
            }

            // Sprawdź czy data przyjazdu nie jest w przeszłości
            if (reservation.getCheckIn().isBefore(LocalDate.now().plusDays(1))) {
                model.addAttribute("error", "Nie można edytować rezerwacji z datą przyjazdu w najbliższym czasie");
                return "redirect:/my-reservations";
            }

            Hotel hotel = reservationService.getHotelById(reservation.getHotelId());
            Room room = roomService.getRoomById(reservation.getRoomId());

            model.addAttribute("reservation", reservation);
            model.addAttribute("hotel", hotel);
            model.addAttribute("room", room);

            return "edit_reservation"; // Szablon dla użytkowników

        } catch (Exception e) {
            model.addAttribute("error", "Błąd przy ładowaniu rezerwacji: " + e.getMessage());
            return "redirect:/my-reservations";
        }
    }

    @PostMapping("/reservation/update/{id}")
    public String updateUserReservation(@PathVariable Long id,
                                        @RequestParam String checkIn,
                                        @RequestParam String checkOut,
                                        RedirectAttributes redirectAttributes) { // ✅ TERAZ BĘDZIE DZIAŁAĆ
        try {
            Reservation existingReservation = reservationService.getReservationById(id);
            if (existingReservation == null) {
                redirectAttributes.addFlashAttribute("error", "Rezerwacja nie została znaleziona");
                return "redirect:/my-reservations";
            }

            // Sprawdź uprawnienia
            String currentUsername = getCurrentUsername(); // ✅ TERAZ BĘDZIE DZIAŁAĆ
            if (!existingReservation.getUsername().equals(currentUsername)) {
                redirectAttributes.addFlashAttribute("error", "Nie masz uprawnień do edycji tej rezerwacji");
                return "redirect:/my-reservations";
            }

            // Walidacja dat
            LocalDate newCheckIn = LocalDate.parse(checkIn);
            LocalDate newCheckOut = LocalDate.parse(checkOut);

            if (newCheckIn.isBefore(LocalDate.now().plusDays(1))) {
                redirectAttributes.addFlashAttribute("error", "Data przyjazdu musi być co najmniej jutro");
                return "redirect:/reservation/edit/" + id;
            }

            if (newCheckOut.isBefore(newCheckIn.plusDays(1))) {
                redirectAttributes.addFlashAttribute("error", "Data wyjazdu musi być co najmniej dzień po przyjeździe");
                return "redirect:/reservation/edit/" + id;
            }

            // Sprawdź dostępność pokoju w nowych datach
            boolean isAvailable = reservationService.isRoomAvailable(
                    existingReservation.getRoomId(),
                    newCheckIn,
                    newCheckOut,
                    existingReservation.getId()
            );

            if (!isAvailable) {
                redirectAttributes.addFlashAttribute("error", "Pokój nie jest dostępny w wybranych datach");
                return "redirect:/reservation/edit/" + id;
            }

            // Przelicz cenę
            Room room = roomService.getRoomById(existingReservation.getRoomId());
            if (room != null) {
                existingReservation.setCheckIn(newCheckIn);
                existingReservation.setCheckOut(newCheckOut);
                existingReservation.calculateTotalPrice(room.getPrice());
            }

            existingReservation.setStartDate(newCheckIn);
            existingReservation.setEndDate(newCheckOut);

            reservationService.updateReservation(existingReservation);
            redirectAttributes.addFlashAttribute("message", "Rezerwacja została zaktualizowana");

            return "redirect:/my-reservations";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas aktualizacji: " + e.getMessage());
            return "redirect:/reservation/edit/" + id;
        }
    }

    // ✅ DODAJ METODĘ getCurrentUsername()
    private String getCurrentUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new RuntimeException("Musisz być zalogowany aby wykonać tę operację");
        }
        return username;
    }
}