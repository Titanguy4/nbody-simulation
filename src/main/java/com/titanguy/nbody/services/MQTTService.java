package com.titanguy.nbody.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class MQTTService {

    private final MessageChannel mqttOutboundChannel;

    public MQTTService(@Qualifier("mqttOutputChannel") MessageChannel mqttOutboundChannel){
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void sendMessage(String topic, String message){
        mqttOutboundChannel.send(MessageBuilder.withPayload(message)
                .setHeader("mqtt_topic", topic)
                .build()
        );
        System.out.println("Message envoyé sur " + topic + " : " + message);
    }
}
