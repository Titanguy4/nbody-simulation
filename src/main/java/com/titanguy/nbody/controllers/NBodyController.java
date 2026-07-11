package com.titanguy.nbody.controllers;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.titanguy.nbody.controllers.dto.BodyDto;
import com.titanguy.nbody.services.DuplicateBodyException;
import com.titanguy.nbody.services.NBodyService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Validated
public class NBodyController {

    private final NBodyService nBodyService;

    public NBodyController(NBodyService nBodyService) {
        this.nBodyService = nBodyService;
    }

    @ServiceActivator(inputChannel = "simulationEventAdd")
    public void addBody(@Payload @Valid BodyDto body) {
        try {
            this.nBodyService.addBody(body);
            log.info("Ajout avec succès du corps {}", body.toString());
        } catch (DuplicateBodyException e) {
            log.warn(e.getMessage());
        }
    }

    @ServiceActivator(inputChannel = "simulationEventMove")
    public void moveBody(@Payload @Valid BodyDto body) {
        try {
            nBodyService.updateBody(body);
            log.info("Mise à jour du corps réussie : {}", body.toString());
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
        }
    }
}
