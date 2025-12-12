
package org.example.apkahotels.controllers;

import org.example.apkahotels.models.*;
import org.example.apkahotels.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;

@Controller
public class ProfileController {

    // ✅ USUŃ InMemoryUserDetailsManager!
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ReservationService reservationService;
    private final HotelService hotelService;
    private final RoomService roomService;

    // ✅ NOWY KONSTRUKTOR BEZ InMemoryUserDetailsManager
    public ProfileController(PasswordEncoder passwordEncoder,
                             UserService userService,
                             ReservationService reservationService,
                             HotelService hotelService,
                             RoomService roomService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.reservationService = reservationService;
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    // Wyświetlanie profilu i formularza zmiany hasła
    @GetMapping("/profile")
    public String showProfile(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("username", username);
        model.addAttribute("passwordUpdateForm", new PasswordUpdateForm());

        // Aktualny użytkownik (jeśli potrzebujesz do wyświetlania profilu)
        AppUser user = userService.getCurrentUser();
        model.addAttribute("user", user);

        // Lista rezerwacji dla zalogowanego użytkownika
        List<Reservation> reservations = reservationService.getReservationsByUsername(username);

        // Mapa: klucz = hotelId, wartość = obiekt Hotel
        Map<Long, Hotel> hotelDetails = new HashMap<>();
        // Mapa: klucz = roomId, wartość = obiekt Room
        Map<Long, Room> roomDetails = new HashMap<>();

        // Dla każdej rezerwacji wczytujemy hotel i pokój, o ile istnieją
        for (Reservation res : reservations) {
            if (res.getHotelId() != null) {
                Hotel h = hotelService.getHotelById(res.getHotelId());
                if (h != null) {
                    hotelDetails.put(res.getHotelId(), h);
                }
            }
            if (res.getRoomId() != null) {
                Room r = roomService.getRoomById(res.getRoomId());
                if (r != null) {
                    roomDetails.put(res.getRoomId(), r);
                }
            }
        }

        // Dodajemy listę rezerwacji i mapy do modelu
        model.addAttribute("reservations", reservations);
        model.addAttribute("hotelDetails", hotelDetails);
        model.addAttribute("roomDetails", roomDetails);

        return "profile";  // widok Thymeleaf (np. profile.html)
    }
    @GetMapping("/cancelReservation/{id}")
    public String cancelReservation(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
        try {
            String currentUsername = authentication.getName();

            Optional<Reservation> reservationOpt = reservationService.findById(id);
            if (reservationOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Rezerwacja nie została znaleziona");
                return "redirect:/my-reservations";
            }

            Reservation reservation = reservationOpt.get();

            if (!reservation.getUsername().equals(currentUsername)) {
                redirectAttributes.addFlashAttribute("error", "Nie masz uprawnień do anulowania tej rezerwacji");
                return "redirect:/my-reservations";
            }

            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("message", "✅ Rezerwacja została anulowana");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Błąd podczas anulowania: " + e.getMessage());
        }

        return "redirect:/my-reservations";
    }



    @GetMapping("/my-reservations")
    public String myReservations(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Reservation> reservations = reservationService.getReservationsByUsername(username);

            // ✅ ZAWSZE INICJALIZUJ MAPY - nawet jeśli puste!
            Map<Long, Hotel> hotelDetails = new HashMap<>();
            Map<Long, Room> roomDetails = new HashMap<>();

            // ✅ DODAJ NULL CHECK dla rezerwacji
            if (reservations != null && !reservations.isEmpty()) {
                for (Reservation res : reservations) {
                    if (res == null) continue; // Skip null reservations

                    // Pobierz hotel
                    if (res.getHotelId() != null) {
                        try {
                            Hotel hotel = hotelService.getHotelById(res.getHotelId());
                            if (hotel != null) {
                                hotelDetails.put(res.getHotelId(), hotel);
                            }
                        } catch (Exception e) {
                            System.err.println("Hotel nie znaleziony: " + res.getHotelId() + " - " + e.getMessage());
                        }
                    }

                    // Pobierz pokój
                    if (res.getRoomId() != null) {
                        try {
                            Room room = roomService.getRoomById(res.getRoomId());
                            if (room != null) {
                                roomDetails.put(res.getRoomId(), room);
                            }
                        } catch (Exception e) {
                            System.err.println("Pokój nie znaleziony: " + res.getRoomId() + " - " + e.getMessage());
                        }
                    }
                }
            } else {
                // Brak rezerwacji - ustaw pustą listę
                reservations = new ArrayList<>();
            }

            // ✅ ZAWSZE dodaj atrybuty do modelu
            model.addAttribute("reservations", reservations);
            model.addAttribute("hotelDetails", hotelDetails);
            model.addAttribute("roomDetails", roomDetails);

            System.out.println("DEBUG: Found " + reservations.size() + " reservations");
            System.out.println("DEBUG: Hotel details size: " + hotelDetails.size());
            System.out.println("DEBUG: Room details size: " + roomDetails.size());

            return "my_reservations";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Błąd przy ładowaniu rezerwacji: " + e.getMessage());
            model.addAttribute("reservations", new ArrayList<>());
            model.addAttribute("hotelDetails", new HashMap<>());
            model.addAttribute("roomDetails", new HashMap<>());
            return "my_reservations"; // ✅ Nie przekierowuj na index!
        }
    }


    // ✅ NOWA ZMIANA HASŁA - PRZEZ BAZĘ DANYCH!
    @PostMapping("/profile/update")
    public String updatePassword(@ModelAttribute("passwordUpdateForm") @Valid PasswordUpdateForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Nowe hasło i potwierdzenie nie są zgodne!");
            return "redirect:/profile";
        }

        try {
            // ✅ POBIERZ UŻYTKOWNIKA Z BAZY DANYCH
            AppUser currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Nie można znaleźć użytkownika!");
                return "redirect:/profile";
            }

            // ✅ SPRAWDŹ STARE HASŁO
            if (!passwordEncoder.matches(form.getOldPassword(), currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Stare hasło jest nieprawidłowe!");
                return "redirect:/profile";
            }

            // ✅ ZAKODUJ I ZAPISZ NOWE HASŁO W BAZIE
            String encodedNewPassword = passwordEncoder.encode(form.getNewPassword());
            currentUser.setPassword(encodedNewPassword);
            userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("message", "Hasło zostało zmienione!");
            return "redirect:/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd przy zmianie hasła: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    // Formularz edycji profilu – przykładowo, edycja danych takich jak email, imię, nazwisko
    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        AppUser user = userService.getCurrentUser();
        model.addAttribute("user", user);
        return "edit_profile"; // widok edit_profile.html
    }

    // Zapis edytowanego profilu
    @PostMapping("/profile/save")
    public String saveProfile(@ModelAttribute("user") AppUser user, RedirectAttributes redirectAttributes) {
        try {
            // ✅ ZAPISZ W BAZIE DANYCH
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("message", "Profil został zaktualizowany.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd przy zapisywaniu profilu: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // Historia rezerwacji użytkownika
    @GetMapping("/profile/reservations")
    public String userReservations(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Reservation> reservations = reservationService.getReservationsByUsername(username);
        model.addAttribute("reservations", reservations);
        return "profile_reservations"; // widok profile_reservations.html
    }
}