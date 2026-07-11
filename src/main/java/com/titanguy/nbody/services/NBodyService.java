package com.titanguy.nbody.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.titanguy.nbody.controllers.dto.BodyDto;
import com.titanguy.nbody.models.Body;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class NBodyService {

    private final List<Body> bodies = new ArrayList<>();

    public void addBody(BodyDto bodyDto) throws DuplicateBodyException {
        Body body = new Body(
                bodyDto.id(),
                bodyDto.type(),
                bodyDto.mass(),
                bodyDto.position(),
                bodyDto.velocity(),
                bodyDto.acceleration());

        if (bodies.stream().anyMatch(existingBody -> existingBody.getId() == body.getId())) {
            throw new DuplicateBodyException("Body with id " + body.getId() + " already exists.");
        }

        bodies.add(body);

        log.debug("Corps ajouté : {}", body);
    }

    public void updateBody(BodyDto bodyDto) {
        Body newBody = new Body(
                bodyDto.id(),
                bodyDto.type(),
                bodyDto.mass(),
                bodyDto.position(),
                bodyDto.velocity(),
                bodyDto.acceleration());

        if (bodies.stream().filter(body -> body.getId() == newBody.getId()).findFirst().isEmpty()) {
            throw new IllegalArgumentException("Body not found");
        }

        // Can do indexOf because Body has overridden equals and hashCode based on id
        bodies.set(bodies.indexOf(newBody), newBody);

        log.debug("Bodie modifié : {}", newBody);
    }

    public void clearBodies() {
        bodies.clear();
    }

    public void deleteBody(Body body) {
        bodies.remove(body);
    }

    public void setSystemSolar() {
        this.clearBodies();
    }
}
