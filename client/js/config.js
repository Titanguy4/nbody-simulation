// Se connecte au broker sur l'hôte qui sert la page ; retombe sur
// localhost quand la page est ouverte en file:// (hostname vide).
const MQTT_BROKER_URL = `ws://${window.location.hostname || "localhost"}:9001`;
const MQTT_TOPIC_IN = "simulation";
const MQTT_TOPIC_ADD = "simulation/event/add";
const MQTT_TOPIC_CLEAR = "simulation/event/clear";
const MQTT_TOPIC_PRESET = "simulation/preset";
const MQTT_TOPIC_PAUSE = "simulation/event/pause";

const COLORS = {
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

function formatMass(kg) {
  return kg.toExponential(2).replace("e+", " × 10^") + " kg";
}

function formatDistance(meters) {
  const au = meters / 1.496e11;
  if (Math.abs(au) >= 0.01) return au.toFixed(3) + " UA";
  return (meters / 1000).toFixed(0) + " km";
}

function formatSpeed(metersPerSecond) {
  return metersPerSecond.toFixed(0) + " m/s";
}
