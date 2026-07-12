package com.titanguy.nbody.services;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
    private final MessageChannel pauseEventChannel;

    private final ThreadPoolTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    public SimulationService(NBodyService nBodyService, MessageChannel mqttOutputChannel,
            MessageChannel pauseEventChannel) {
        this.nBodyService = nBodyService;
        this.mqttOutputChannel = mqttOutputChannel;
        this.pauseEventChannel = pauseEventChannel;

        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(1);
        this.taskScheduler.setThreadNamePrefix("Nbody-Cluster-");
        this.taskScheduler.initialize();
    }

    @PostConstruct
    private void init() {
        Presets preset = new Presets(nBodyService);
        preset.setSystemSolar();
        startSimulation(Instant.now().plusSeconds(2));

        log.info("SimulationService initialized with system solar bodies.");
    }

    private synchronized void startSimulation(Instant startTime) {
        if (this.scheduledTask == null || this.scheduledTask.isCancelled()) {

            this.scheduledTask = this.taskScheduler.scheduleAtFixedRate(
                    this::update,
                    startTime,
                    java.time.Duration.ofMillis(UPDATE_INTERVAL_MS));
        }
    }

    @ServiceActivator(inputChannel = "pauseEventChannel")
    private synchronized void toggleRunningSimulation(Message<String> message) {
        String payload = message.getPayload().trim().toUpperCase();

        if (payload.equals("PLAY")) {
            startSimulation(Instant.now());
        } else if (payload.equals("STOP")) {
            if (this.scheduledTask != null && !this.scheduledTask.isCancelled()) {
                this.scheduledTask.cancel(false);
            }
        } else {
            log.warn("Message non compréhensible lors de l'évènement pause");

        }
    }

    private void update() {
        List<Body> bodies = nBodyService.getBodies();

        // Virtual time, time is elapsing during UPDATE_INTERVAL_MS. Here 24 * 60 * 60,
        // 12 hours
        double deltaTime = 43200.0;
        // Plummer softening, to avoid divided by 0 when colision
        double eps = 1e3;

        Vector2D[] newAccelerations = new Vector2D[bodies.size()];

        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2D force = new Vector2D(0, 0);

            for (Body other : bodies) {
                if (!body.equals(other)) {
                    Vector2D coordinateVector = other.getPosition().subtract(body.getPosition());
                    double distance = coordinateVector.distance();

                    if (distance > 0) {
                        double forceValue = G * body.getMass() * other.getMass() / ((distance * distance) + eps);
                        Vector2D forceVector = coordinateVector.normalize().multipleByScalar(forceValue);
                        force = force.add(forceVector);
                    }
                }
            }
            newAccelerations[i] = force.multipleByScalar(1 / body.getMass());
        }

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
