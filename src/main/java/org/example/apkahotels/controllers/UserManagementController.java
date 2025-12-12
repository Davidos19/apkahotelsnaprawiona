package org.example.apkahotels.controllers;

import org.example.apkahotels.models.AppUser;
import org.example.apkahotels.models.UserRole;
import org.example.apkahotels.services.SecurityService;
import org.example.apkahotels.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;
    private final SecurityService securityService;

    public UserManagementController(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    @GetMapping
    public String listUsers(Model model) {
        try {
            List<AppUser> users = userService.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("userRoles", UserRole.values());
            return "admin/users"; // Szukaj template admin/users.html
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Błąd przy ładowaniu użytkowników: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    @PostMapping("/{userId}/role")
    public String changeUserRole(@PathVariable Long userId,
                                 @RequestParam UserRole newRole,
                                 RedirectAttributes redirectAttributes) {
        try {
            //  securityService.checkPermission(UserRole.ADMIN);//
            userService.changeUserRole(userId, newRole);
            redirectAttributes.addFlashAttribute("message", "Rola użytkownika została zmieniona!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            AppUser user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Użytkownik nie znaleziony");
                return "redirect:/admin/hotels/users"; // ✅ ZMIEŃ NA WŁAŚCIWĄ ŚCIEŻKĘ
            }

            user.setActive(!user.isActive());
            userService.saveUser(user);

            String status = user.isActive() ? "aktywowany" : "dezaktywowany";
            redirectAttributes.addFlashAttribute("message", "Użytkownik został " + status);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd: " + e.getMessage());
        }

        return "redirect:/admin/hotels/users"; // ✅ ZMIEŃ NA WŁAŚCIWĄ ŚCIEŻKĘ
    }

}