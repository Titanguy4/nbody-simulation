package com.titanguy.nbody.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
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
    public MqttPahoClientFactory mqttClientFactory(){
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);
        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean MessageChannel mqttInputChannel() { return new DirectChannel(); }

    @Bean MessageChannel mqttOutputChannel(){
        return new DirectChannel();
    }

    @Bean MessageChannel simulationEventAdd() { return new DirectChannel(); }

    @Bean MessageChannel simulationEventMove() { return new DirectChannel(); }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter debugAdapter() {
        var adapter = new MqttPahoMessageDrivenChannelAdapter("debug-client", mqttClientFactory(), "#");
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter adapterAdd(){
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId,
                        mqttClientFactory(),
                        topicEventAdd
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(simulationEventAdd());

        return adapter;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter adapterMove(){
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId,
                        mqttClientFactory(),
                        topicEventMove
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(simulationEventMove());

        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            System.out.println("🪵 [DEBUG MQTT] Topic: " + message.getHeaders().get("mqtt_receivedTopic"));
            System.out.println("🪵 Payload: " + message.getPayload());
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound(){
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(outgoingTopic);

        return messageHandler;
    }

}
