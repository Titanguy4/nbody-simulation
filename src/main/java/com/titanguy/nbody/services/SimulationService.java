package com.titanguy.nbody.services;

import java.util.List;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.titanguy.nbody.models.Body;
import com.titanguy.nbody.models.Vector2D;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class SimulationService {

    private final NBodyService nBodyService;
    private final MessageChannel mqttOutputChannel;

    private static final double G = 6.67430e-11;
    private static final long UPDATE_INTERVAL_MS = 100;

    public SimulationService(NBodyService nBodyService, MessageChannel mqttOutputChannel) {
        this.nBodyService = nBodyService;
        this.mqttOutputChannel = mqttOutputChannel;
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    public void update() {
        List<Body> bodies = nBodyService.getBodies();
        double deltaTime = 1.0;

        for (Body body : bodies) {
            Vector2D force = new Vector2D(0, 0);

            for (Body other : bodies) {
                if (!body.equals(other)) {
                    Vector2D coordinateVector = other.getPosition().subtract(body.getPosition());
                    double distance = coordinateVector.distance();

                    if (distance != 0) {
                        double forceValue = G * body.getMass() * other.getMass() / (distance * distance);
                        Vector2D forceVector = coordinateVector.normalize().multipleByScalar(forceValue);
                        force.add(forceVector);
                    }
                }
            }

            Vector2D acceleration = force.multipleByScalar(1 / body.getMass());
            body.setAcceleration(acceleration);
            body.getVelocity().add(acceleration.multipleByScalar(deltaTime));
            body.getPosition().add(body.getVelocity().multipleByScalar(deltaTime));
        }
        String jsonBodies = convertBodiesToJson(bodies);
        log.info(jsonBodies);
        Message<String> message = MessageBuilder.withPayload(jsonBodies).build();
        mqttOutputChannel.send(message);
        log.info("Corps envoyé sur simulation topic");
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
