// Wiring only: instantiates the mqtt client, renderer and panel and
// connects their callbacks to each other. This is the only file that
// knows about all the components.
import { refreshIcons } from "./icons";
import { MqttClient } from "./mqtt-client";
import { Renderer } from "./renderer";
import { Panel } from "./panel";
import { bindInteractions } from "./interactions";

refreshIcons();

const mqttClient = new MqttClient();
const renderer = new Renderer(
  document.getElementById("space") as HTMLCanvasElement,
);
const panel = new Panel();

mqttClient.onConnect = () => panel.setConnected();
mqttClient.onBodiesUpdate = (bodies) => {
  panel.setBodyCount(bodies.length);
  renderer.drawUniverse(bodies);
};

panel.onToggle(() => {
  const isPlaying = mqttClient.togglePause();
  panel.setPlaying(isPlaying);
});

panel.onClear(() => {
  mqttClient.clearUniverse();
  renderer.resetView();
  renderer.clear();
});

panel.onPresetSelect((presetName) => {
  mqttClient.loadPreset(presetName);
  renderer.resetView();
  renderer.clear();
});

bindInteractions(mqttClient, renderer, panel);
