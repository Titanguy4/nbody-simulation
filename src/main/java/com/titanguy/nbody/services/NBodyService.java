package com.titanguy.nbody.services;

import com.titanguy.nbody.models.Body;

import java.util.ArrayList;
import java.util.List;

public class NBodyService {

    private final List<Body> bodies = new ArrayList<>();

    public void addBody(Body body){
        if(body == null){
            throw new IllegalArgumentException("Body cannot be null");
        }
        bodies.add(body);
    }

    public List<Body> getBodies(){
        return bodies;
    }

    public void clearBodies(){
        bodies.clear();
    }

    public void deleteBody(Body body){
        bodies.remove(body);
    }

    public void updateBody(Body body){
        if(body == null){
            throw new IllegalArgumentException("Body cannot be null");
        }
        if(!bodies.contains(body)){
            throw new IllegalArgumentException("Body not found");
        }
        bodies.set(bodies.indexOf(body), body);
    }

    public void setSystemSolar(){
        this.clearBodies();
    }
}
