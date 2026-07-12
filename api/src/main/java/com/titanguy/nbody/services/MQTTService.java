package com.titanguy.nbody.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MQTTService {

    private final MessageChannel mqttOutboundChannel;

    public MQTTService(@Qualifier("mqttOutputChannel") MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void sendMessage(String topic, String message) {
        mqttOutboundChannel.send(MessageBuilder.withPayload(message)
                .setHeader("mqtt_topic", topic)
                .build());
        log.info("Message envoyé sur " + topic + " : " + message);
    }
}
