
package org.example.apkahotels.controllers;

import org.example.apkahotels.services.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/emails")
@PreAuthorize("hasRole('ADMIN')") // ✅ UŻYJ SPRING SECURITY
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/test")
    public String showEmailTestPage(Model model) {
        return "admin/email-test"; // ✅ USUŃ SecurityService
    }

    @PostMapping("/test/send")
    public String sendTestEmail(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            emailService.sendTestEmail(email);
            redirectAttributes.addFlashAttribute("message", "✅ Email testowy wysłany na: " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Błąd wysyłania: " + e.getMessage());
        }

        return "redirect:/admin/emails/test";
    }
}