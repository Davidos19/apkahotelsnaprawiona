
package org.example.apkahotels.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.example.apkahotels.models.Hotel;
import org.example.apkahotels.models.Reservation;
import org.example.apkahotels.models.Room;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender, Configuration freemarkerConfig) {
        this.mailSender = mailSender;
        this.freemarkerConfig = freemarkerConfig;
    }

    // ✅ POTWIERDZENIE REZERWACJI
    public void sendBookingConfirmation(String toEmail, Reservation reservation, Hotel hotel, Room room) {
        if (!emailEnabled) {
            System.out.println("📧 EMAIL DISABLED - Would send booking confirmation to: " + toEmail);
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("reservation", reservation);
            model.put("hotel", hotel);
            model.put("room", room);
            model.put("userName", reservation.getUsername());
            model.put("totalNights", calculateNights(reservation.getCheckIn(), reservation.getCheckOut()));
            model.put("formattedCheckIn", reservation.getCheckIn().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            model.put("formattedCheckOut", reservation.getCheckOut().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

            String subject = "✅ Potwierdzenie rezerwacji - " + hotel.getName();
            String content = processTemplate("booking-confirmation.ftl", model);

            sendEmail(toEmail, subject, content);

            System.out.println("📧 Wysłano potwierdzenie rezerwacji do: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłania email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ ANULOWANIE REZERWACJI
    public void sendBookingCancellation(String toEmail, Reservation reservation, Hotel hotel) {
        if (!emailEnabled) {
            System.out.println("📧 EMAIL DISABLED - Would send cancellation to: " + toEmail);
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("reservation", reservation);
            model.put("hotel", hotel);
            model.put("userName", reservation.getUsername());
            model.put("formattedCheckIn", reservation.getCheckIn().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            model.put("formattedCheckOut", reservation.getCheckOut().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

            String subject = "❌ Anulowanie rezerwacji - " + hotel.getName();
            String content = processTemplate("booking-cancellation.ftl", model);

            sendEmail(toEmail, subject, content);

            System.out.println("📧 Wysłano anulowanie rezerwacji do: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłania email: " + e.getMessage());
        }
    }

    // ✅ PRZYPOMNIENIE O PRZYJEŹDZIE
    public void sendCheckInReminder(String toEmail, Reservation reservation, Hotel hotel, Room room) {
        if (!emailEnabled) {
            System.out.println("📧 EMAIL DISABLED - Would send check-in reminder to: " + toEmail);
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("reservation", reservation);
            model.put("hotel", hotel);
            model.put("room", room);
            model.put("userName", reservation.getUsername());
            model.put("formattedCheckIn", reservation.getCheckIn().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            model.put("daysUntilCheckIn", LocalDate.now().until(reservation.getCheckIn()).getDays());

            String subject = "🏨 Przypomnienie o przyjeździe - " + hotel.getName();
            String content = processTemplate("check-in-reminder.ftl", model);

            sendEmail(toEmail, subject, content);

            System.out.println("📧 Wysłano przypomnienie do: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłania email: " + e.getMessage());
        }
    }

    // ✅ EMAIL POWITALNY
    public void sendWelcomeEmail(String toEmail, String userName) {
        if (!emailEnabled) {
            System.out.println("📧 EMAIL DISABLED - Would send welcome email to: " + toEmail);
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("userName", userName);

            String subject = "🎉 Witamy w naszym systemie rezerwacji!";
            String content = processTemplate("welcome-email.ftl", model);

            sendEmail(toEmail, subject, content);

            System.out.println("📧 Wysłano email powitalny do: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłania email: " + e.getMessage());
        }
    }

    // ✅ TEST EMAIL
    public void sendTestEmail(String toEmail) {
        if (!emailEnabled) {
            System.out.println("📧 EMAIL DISABLED - Would send test email to: " + toEmail);
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("testMessage", "Email system działa prawidłowo!");

            String subject = "🧪 Test Email System";
            String content = processTemplate("test-email.ftl", model);

            sendEmail(toEmail, subject, content);

            System.out.println("📧 Wysłano email testowy do: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Błąd wysyłania email: " + e.getMessage());
        }
    }

    // ✅ POMOCNICZE METODY
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setFrom(fromEmail, fromName); // ✅ OBSŁUŻONE WYJĄTEK

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            // ✅ FALLBACK BEZ NAZWY NADAWCY
            System.err.println("⚠️ Błąd kodowania nazwy nadawcy, wysyłam bez nazwy: " + e.getMessage());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setFrom(fromEmail); // BEZ NAZWY NADAWCY

            mailSender.send(message);
        }
    }

    private String processTemplate(String templateName, Map<String, Object> model) throws Exception {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    private long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        return checkIn.until(checkOut).getDays();
    }
}