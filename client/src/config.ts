// MQTT topic/broker constants and the body-type color map.

// Connects to the broker on the host that serves the page; falls back to
// localhost during local development.
export const MQTT_BROKER_URL = `ws://${window.location.hostname || "localhost"}:9001`;
export const MQTT_TOPIC_IN = "simulation";
export const MQTT_TOPIC_ADD = "simulation/event/add";
export const MQTT_TOPIC_CLEAR = "simulation/event/clear";
export const MQTT_TOPIC_PRESET = "simulation/preset";
export const MQTT_TOPIC_PAUSE = "simulation/event/pause";

export const COLORS: Record<string, string> = {
  Sun: "#ffcc00",
  Mercury: "#a8a8a8",
  Venus: "#e08f46",
  Earth: "#4b90ff",
  Mars: "#ff4a2b",
  Jupiter: "#cda67d",
  Saturn: "#e2ddaa",
  Uranus: "#82d1ff",
  Neptune: "#3e54e8",
  PLANET: "#00ff88",
  STAR: "#ff4444",
  ASTEROID: "#aaaaaa",
};
