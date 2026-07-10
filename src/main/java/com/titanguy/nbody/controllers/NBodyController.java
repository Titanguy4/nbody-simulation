package com.titanguy.nbody.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import com.titanguy.nbody.models.Body;
import com.titanguy.nbody.services.NBodyService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class NBodyController {

    private final ObjectMapper objectMapper;
    private final NBodyService nBodyService;

    public NBodyController(NBodyService nBodyService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.nBodyService = nBodyService;
    }

    @ServiceActivator(inputChannel = "simulationEventAdd")
    public ResponseEntity<String> addBody(String bodyJson) {
        try {
            Body body = objectMapper.readValue(bodyJson, Body.class);
            this.nBodyService.addBody(body);
        } catch (JacksonException e) {
            log.error("Erreur de désérialisation du JSON pour l'ajout du corps : {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Erreur de format JSON");
        } catch (Exception e) {
            log.error("Erreur inconnue lors de l'ajout d'un corp : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur interne");
        }

        return ResponseEntity.status(200).body("Ajout succès");
    }

    @ServiceActivator(inputChannel = "simulationEventMove")
    public ResponseEntity<String> moveBody(String bodyJson) {
        try {
            Body body = objectMapper.readValue(bodyJson, Body.class);
            nBodyService.updateBody(body);
        } catch (JacksonException e) {
            log.error("Erreur de désérialisation du JSON pour movement du corps : {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Erreur de format JSON");
        } catch (Exception e) {
            log.error("Erreur inconnue lors du movement d'un corp : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur interne");
        }

        return ResponseEntity.status(200).body("Movement succès");
    }
}
