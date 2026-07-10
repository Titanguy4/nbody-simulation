package com.titanguy.nbody.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.titanguy.nbody.models.Body;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NBodyService {

    private final List<Body> bodies = new ArrayList<>();

    public void addBody(Body body) {
        if (body == null) {
            throw new IllegalArgumentException("Body cannot be null");
        }
        bodies.add(body);
        log.warn("Bodie ajouté : {}", bodies);
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public void clearBodies() {
        bodies.clear();
    }

    public void deleteBody(Body body) {
        bodies.remove(body);
    }

    public void updateBody(Body body) {
        if (body == null) {
            throw new IllegalArgumentException("Body cannot be null");
        }
        if (!bodies.contains(body)) {
            throw new IllegalArgumentException("Body not found");
        }
        bodies.set(bodies.indexOf(body), body);
    }

    public void setSystemSolar() {
        this.clearBodies();
    }
}
