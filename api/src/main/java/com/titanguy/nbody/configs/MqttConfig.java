package com.titanguy.nbody.configs;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.mqtt.core.Mqttv5ClientManager;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.StringMessageConverter;

import com.titanguy.nbody.controllers.dto.BodyDto;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.topic.outgoing.simulation}")
    private String outgoingTopic;

    @Value("${mqtt.topic.incoming.simulation-event-add}")
    private String topicEventAdd;

    @Value("${mqtt.topic.incoming.simulation-event-move}")
    private String topicEventMove;

    @Value("${mqtt.topic.incoming.simulation-event-delete}")
    private String topicEventDelete;

    @Value("${mqtt.topic.incoming.simulation-event-pause}")
    private String topicEventPause;

    @Value("${mqtt.topic.incoming.simulation-preset}")
    private String topicPreset;

    @Bean
    public Mqttv5ClientManager mqttClientManager() {
        MqttConnectionOptions options = new MqttConnectionOptions();

        options.setServerURIs(new String[] { brokerUrl });
        options.setCleanStart(true);

        return new Mqttv5ClientManager(options, clientId);
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttErrorChannel() {
        return new DirectChannel();
    }

    // --- Channels for ADD ---
    @Bean
    public MessageChannel simulationEventAddRaw() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel simulationEventAdd() {
        return new DirectChannel();
    }

    // --- Channels for MOVE ---
    @Bean
    public MessageChannel simulationEventMoveRaw() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel simulationEventMove() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel pauseEventChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel presetChannel() {
        return new DirectChannel();
    }

    /** Receive add events from MQTT. */
    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter adapterAdd(Mqttv5ClientManager clientManager) {
        Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(clientManager,
                topicEventAdd);

        adapter.setCompletionTimeout(5000);
        adapter.setQos(qos);
        adapter.setMessageConverter(new StringMessageConverter());
        adapter.setErrorChannel(mqttErrorChannel());
        adapter.setOutputChannel(simulationEventAddRaw());

        return adapter;
    }

    /** Receive move events from MQTT. */
    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter adapterMove(Mqttv5ClientManager clientManager) {
        Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(clientManager,
                topicEventMove);

        adapter.setCompletionTimeout(5000);
        adapter.setMessageConverter(new StringMessageConverter());
        adapter.setQos(qos);
        adapter.setErrorChannel(mqttErrorChannel());
        adapter.setOutputChannel(simulationEventMoveRaw());

        return adapter;
    }

    @Bean
    @Transformer(inputChannel = "simulationEventAddRaw", outputChannel = "simulationEventAdd")
    public JsonToObjectTransformer jsonToBodyDtoAddTransformer() {
        return new JsonToObjectTransformer(BodyDto.class);
    }

    @Bean
    @Transformer(inputChannel = "simulationEventMoveRaw", outputChannel = "simulationEventMove")
    public JsonToObjectTransformer jsonToBodyDtoMoveTransformer() {
        return new JsonToObjectTransformer(BodyDto.class);
    }

    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter adapterPause(Mqttv5ClientManager clientManager) {
        Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(clientManager,
                topicEventPause);

        adapter.setCompletionTimeout(5000);
        adapter.setQos(qos);
        adapter.setMessageConverter(new StringMessageConverter());
        adapter.setErrorChannel(mqttErrorChannel());
        adapter.setOutputChannel(pauseEventChannel());

        return adapter;
    }

    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter adapterPreset(Mqttv5ClientManager clientManager) {
        Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(clientManager,
                topicPreset);

        adapter.setCompletionTimeout(5000);
        adapter.setQos(qos);
        adapter.setMessageConverter(new StringMessageConverter());
        adapter.setErrorChannel(mqttErrorChannel());
        adapter.setOutputChannel(presetChannel());

        return adapter;
    }

    /** Send outgoing simulation messages to MQTT. */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound(Mqttv5ClientManager clientManager) {
        Mqttv5PahoMessageHandler messageHandler = new Mqttv5PahoMessageHandler(clientManager);

        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(outgoingTopic);
        messageHandler.setDefaultQos(qos);

        return messageHandler;
    }

    /** Log incoming MQTT messages for debugging. */
    @Bean
    @Profile("DEBUG")
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            log.info("🪵 [DEBUG MQTT] Topic: {} ", message.getHeaders().get("mqtt_receivedTopic"));

            Object payload = message.getPayload();
            if (payload instanceof byte[] bytes) {
                log.info("🪵 Payload: {} ", new String(bytes));
            }
        };
    }

    /** Listen to all MQTT topics in DEBUG mode. */
    @Bean
    @Profile("DEBUG")
    public Mqttv5PahoMessageDrivenChannelAdapter debugAdapter(Mqttv5ClientManager clientManager) {
        var adapter = new Mqttv5PahoMessageDrivenChannelAdapter(clientManager, "#");

        adapter.setOutputChannel(mqttInputChannel());
        adapter.setMessageConverter(new StringMessageConverter());

        return adapter;
    }

}
