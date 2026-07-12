// Transport: wraps the raw mqtt.js connection and exposes simulation-specific
// publish calls plus a bodies-update callback. No DOM or canvas access.
class MqttClient {
  constructor() {
    this.isPlaying = true;
    this.onConnect = null;
    this.onBodiesUpdate = null;

    this.client = mqtt.connect(MQTT_BROKER_URL);

    this.client.on("connect", () => {
      this.client.subscribe(MQTT_TOPIC_IN);
      if (this.onConnect) this.onConnect();
    });

    this.client.on("message", (topic, message) => {
      if (topic !== MQTT_TOPIC_IN) return;
      try {
        const bodies = JSON.parse(message.toString());
        if (this.onBodiesUpdate) this.onBodiesUpdate(bodies);
      } catch (e) {
        console.error("Erreur MQTT :", e);
      }
    });
  }

  togglePause() {
    this.isPlaying = !this.isPlaying;
    this.client.publish(MQTT_TOPIC_PAUSE, this.isPlaying ? "PLAY" : "STOP");
    return this.isPlaying;
  }

  addBody(bodyDto) {
    this.client.publish(MQTT_TOPIC_ADD, JSON.stringify(bodyDto));
  }

  clearUniverse() {
    this.client.publish(MQTT_TOPIC_CLEAR, "{}");
  }

  loadPreset(presetName) {
    this.client.publish(MQTT_TOPIC_PRESET, presetName);
  }
}
