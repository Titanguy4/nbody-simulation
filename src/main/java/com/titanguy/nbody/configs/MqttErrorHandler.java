package com.titanguy.nbody.configs;

import java.util.stream.Collectors;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MqttErrorHandler {

    @ServiceActivator(inputChannel = "mqttErrorChannel")
    public void handleMqttError(Message<MessagingException> errorMessage) {

        Throwable cause = errorMessage.getPayload();

        while (cause != null) {

            if (cause instanceof ConstraintViolationException validationEx) {
                String errorDetails = validationEx.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining(", "));

                log.warn("Corps céleste rejeté (Données invalides) : {}", errorDetails);
                return;
            }

            else if (cause instanceof MessageConversionException || cause instanceof IllegalArgumentException) {
                log.error("Impossible de lire le message MQTT (Format JSON invalide). Détail : {}",
                        cause.getMessage());
                return;
            }

            cause = cause.getCause();
        }

        log.error("Erreur inattendue dans le flux MQTT : {}", errorMessage.getPayload());
    }
}