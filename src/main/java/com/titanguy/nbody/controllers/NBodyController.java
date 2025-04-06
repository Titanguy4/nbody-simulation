package com.titanguy.nbody.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.titanguy.nbody.models.Body;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class NBodyController {

    private ObjectMapper objectMapper;

    public NBodyController(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @ServiceActivator(inputChannel = "simulationEventAdd")
    public void addBody(String bodyJson){
        try {
            Body body = objectMapper.readValue(bodyJson, Body.class);
            System.out.println(body);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @ServiceActivator(inputChannel = "simulationEventMove")
    public void moveBody(String bodyJson){
        try{
            Body body = objectMapper.readValue(bodyJson, Body.class);
            System.out.println(body);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
