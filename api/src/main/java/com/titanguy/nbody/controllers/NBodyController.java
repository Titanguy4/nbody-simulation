package com.titanguy.nbody.controllers;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.titanguy.nbody.controllers.dto.BodyDto;
import com.titanguy.nbody.services.BodyLimitExceededException;
import com.titanguy.nbody.services.DuplicateBodyException;
import com.titanguy.nbody.services.NBodyService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Validated
public class NBodyController {

    // Le broker est anonyme : impossible de limiter par client, donc la limite
    // est globale — elle borne le débit total d'ajouts quel que soit l'émetteur.
    private static final int MAX_ADDS_PER_SECOND = 10;

    private final NBodyService nBodyService;

    private long rateWindowStartMs = 0;
    private int addsInWindow = 0;

    public NBodyController(NBodyService nBodyService) {
        this.nBodyService = nBodyService;
    }

    @ServiceActivator(inputChannel = "simulationEventAdd")
    public void addBody(@Payload @Valid BodyDto body) {
        if (!allowAdd()) {
            return;
        }
        try {
            this.nBodyService.addBody(body);
            log.info("Ajout avec succès du corps {}", body.toString());
        } catch (DuplicateBodyException | BodyLimitExceededException e) {
            log.warn(e.getMessage());
        }
    }

    private synchronized boolean allowAdd() {
        long now = System.currentTimeMillis();
        if (now - rateWindowStartMs >= 1000) {
            rateWindowStartMs = now;
            addsInWindow = 0;
        }
        addsInWindow++;
        if (addsInWindow == MAX_ADDS_PER_SECOND + 1) {
            log.warn("Limite de {} ajouts/seconde atteinte, ajouts ignorés jusqu'à la prochaine fenêtre", MAX_ADDS_PER_SECOND);
        }
        return addsInWindow <= MAX_ADDS_PER_SECOND;
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
