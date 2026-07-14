// Transport: wraps the raw mqtt.js connection and exposes simulation-specific
// publish calls plus a bodies-update callback. No DOM or canvas access.
import mqtt, { type MqttClient as MqttConnection } from "mqtt";
import {
  MQTT_BROKER_URL,
  MQTT_TOPIC_ADD,
  MQTT_TOPIC_CLEAR,
  MQTT_TOPIC_IN,
  MQTT_TOPIC_PAUSE,
  MQTT_TOPIC_PRESET,
} from "./config";
import type { Body } from "./types";

export class MqttClient {
  isPlaying = true;
  onConnect: (() => void) | null = null;
  onBodiesUpdate: ((bodies: Body[]) => void) | null = null;

  private readonly client: MqttConnection;

  constructor() {
    this.client = mqtt.connect(MQTT_BROKER_URL);

    this.client.on("connect", () => {
      this.client.subscribe(MQTT_TOPIC_IN);
      if (this.onConnect) this.onConnect();
    });

    this.client.on("message", (topic, message) => {
      if (topic !== MQTT_TOPIC_IN) return;
      try {
        const bodies = JSON.parse(message.toString()) as Body[];
        if (this.onBodiesUpdate) this.onBodiesUpdate(bodies);
      } catch (e) {
        console.error("Erreur MQTT :", e);
      }
    });
  }

  togglePause(): boolean {
    this.isPlaying = !this.isPlaying;
    this.client.publish(MQTT_TOPIC_PAUSE, this.isPlaying ? "PLAY" : "STOP");
    return this.isPlaying;
  }

  addBody(bodyDto: Body): void {
    this.client.publish(MQTT_TOPIC_ADD, JSON.stringify(bodyDto));
  }

  clearUniverse(): void {
    this.client.publish(MQTT_TOPIC_CLEAR, "{}");
  }

  loadPreset(presetName: string): void {
    this.client.publish(MQTT_TOPIC_PRESET, presetName);
  }
}
