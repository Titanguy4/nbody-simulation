// Wires the mqtt client, renderer and panel together. This is the only
// file that knows about all three.
lucide.createIcons();

const mqttClient = new MqttClient();
const renderer = new Renderer(document.getElementById("space"));
const panel = new Panel();

mqttClient.onConnect = () => panel.setConnected();
mqttClient.onBodiesUpdate = (bodies) => {
  panel.setBodyCount(bodies.length);
  renderer.drawUniverse(bodies);
};

window.addEventListener("wheel", (event) => renderer.zoom(event.deltaY));

panel.onToggle(() => {
  const isPlaying = mqttClient.togglePause();
  panel.setPlaying(isPlaying);
});

panel.onClear(() => {
  mqttClient.clearUniverse();
  renderer.clear();
});

panel.onPresetSelect((presetName) => {
  mqttClient.loadPreset(presetName);
  renderer.clear();
});

renderer.canvas.addEventListener("mousemove", (event) => {
  const hovered = renderer.getBodyAt(event.clientX, event.clientY);
  if (hovered) panel.showTooltip(hovered, event.clientX, event.clientY);
  else panel.hideTooltip();
});

renderer.canvas.addEventListener("mouseleave", () => panel.hideTooltip());

renderer.canvas.addEventListener("click", (event) => {
  const position = renderer.screenToWorld(event.clientX, event.clientY);
  const form = panel.getFormValues();
  mqttClient.addBody({
    id: Math.floor(Math.random() * 1000000),
    type: { name: form.type },
    mass: form.mass,
    position,
    velocity: { x: form.vx, y: form.vy },
    acceleration: { x: 0.0, y: 0.0 },
  });
});
