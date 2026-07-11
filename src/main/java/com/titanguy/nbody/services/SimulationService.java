package com.titanguy.nbody.services;

import java.util.List;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.titanguy.nbody.configs.Presets;
import com.titanguy.nbody.models.Body;
import com.titanguy.nbody.models.Vector2D;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class SimulationService {

    private static final double G = 6.67430e-11;
    private static final long UPDATE_INTERVAL_MS = 10;

    private final NBodyService nBodyService;
    private final MessageChannel mqttOutputChannel;

    public SimulationService(NBodyService nBodyService, MessageChannel mqttOutputChannel) {
        this.nBodyService = nBodyService;
        this.mqttOutputChannel = mqttOutputChannel;
    }

    @PostConstruct
    public void init() {
        Presets preset = new Presets(nBodyService);
        preset.setPlanetaryRing();

        log.info("SimulationService initialized with system solar bodies.");
    }

    @Scheduled(initialDelay = 2000, fixedRate = UPDATE_INTERVAL_MS)
    public void update() {
        List<Body> bodies = nBodyService.getBodies();

        // 🕒 1 heure par calcul (3600s) au lieu d'une journée. Plus stable !
        double deltaTime = 86400.0;
        double eps = 1e3; // Sécurité anti-division par zéro (1000 mètres minimum perçus)

        Vector2D[] newAccelerations = new Vector2D[bodies.size()];

        // 1. Calcul des forces (sans toucher aux positions !)
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2D force = new Vector2D(0, 0);

            for (Body other : bodies) {
                if (!body.equals(other)) {
                    Vector2D coordinateVector = other.getPosition().subtract(body.getPosition());
                    double distance = coordinateVector.distance();

                    if (distance > 0) {
                        // ✅ Sécurité : on ajoute 'eps' pour éviter l'explosion si d approche 0
                        double forceValue = G * body.getMass() * other.getMass() / ((distance * distance) + eps);
                        Vector2D forceVector = coordinateVector.normalize().multipleByScalar(forceValue);
                        force = force.add(forceVector);
                    }
                }
            }
            newAccelerations[i] = force.multipleByScalar(1 / body.getMass());
        }

        // 2. Application des mouvements
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2D acceleration = newAccelerations[i];

            body.setAcceleration(acceleration);

            Vector2D newVelocity = body.getVelocity().add(acceleration.multipleByScalar(deltaTime));
            body.setVelocity(newVelocity);

            Vector2D newPosition = body.getPosition().add(newVelocity.multipleByScalar(deltaTime));
            body.setPosition(newPosition);
        }

        String jsonBodies = convertBodiesToJson(bodies);
        log.debug(jsonBodies);

        Message<String> message = MessageBuilder.withPayload(jsonBodies).build();
        mqttOutputChannel.send(message);
    }

    private String convertBodiesToJson(List<Body> bodies) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(bodies);
        } catch (JacksonException e) {
            log.error("Erreur lors de la conversion des corps en JSON pour l'envoie", e.getMessage(), e);
            return "[]";
        }
    }
}
