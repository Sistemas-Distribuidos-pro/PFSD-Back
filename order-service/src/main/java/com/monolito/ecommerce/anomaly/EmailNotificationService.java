package com.monolito.ecommerce.anomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String destinatario;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${alerts.email.destinatario:admin@ecommerce.com}") String destinatario) {
        this.mailSender = mailSender;
        this.destinatario = destinatario;
    }

    public void enviarAlerta(AlertEvent alert) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Anomalía detectada — Orden #" + alert.orderId());
            mensaje.setText(construirCuerpo(alert));
            mailSender.send(mensaje);
            log.info("Email de alerta enviado para orderId={}", alert.orderId());
        } catch (Exception e) {
            log.error("No se pudo enviar email para orderId={}: {}", alert.orderId(), e.getMessage());
        }
    }

    private static String construirCuerpo(AlertEvent alert) {
        return """
                Se detectó una anomalía en la siguiente orden:

                Orden ID  : %d
                Usuario ID: %d
                Total     : $%s
                Detectada : %s

                Razones:
                %s
                """.formatted(
                alert.orderId(),
                alert.userId(),
                alert.total(),
                alert.detectedAt(),
                String.join("\n", alert.razones().stream().map(r -> "  • " + r).toList())
        );
    }
}
