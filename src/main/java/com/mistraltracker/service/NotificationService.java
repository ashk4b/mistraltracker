package com.mistraltracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendStormAlert(String recipientEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("mistraltracker@gmail.com");
            message.setTo(recipientEmail);
            message.setSubject("🚨 ALERTE ROUGE - MISTRAL TRACKER");
            message.setText("ATTENTION !\n\n" +
                    "Une simulation de tempête a été déclenchée sur la station Plages du Prado.\n" +
                    "Vent > 85 km/h détecté.\n\n" +
                    "Veuillez évacuer le plan d'eau immédiatement.\n\n" +
                    "--\n" +
                    "Système MistralTracker");

            emailSender.send(message);
            System.out.println("Email d'alerte envoyé avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du mail : " + e.getMessage());
        }
    }
}