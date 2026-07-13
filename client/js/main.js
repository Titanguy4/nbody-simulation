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
  // Clicking/tapping an existing body shows its info instead of stacking
  // a new body on top — on touch screens this is the only tooltip access.
  const tapped = renderer.getBodyAt(event.clientX, event.clientY);
  if (tapped) {
    panel.showTooltip(tapped, event.clientX, event.clientY);
    return;
  }
  panel.hideTooltip();

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

// Pinch-to-zoom on touch screens, the counterpart of the wheel listener.
let pinchDistance = null;
const touchesDistance = (touches) =>
  Math.hypot(
    touches[0].clientX - touches[1].clientX,
    touches[0].clientY - touches[1].clientY,
  );

renderer.canvas.addEventListener("touchstart", (event) => {
  if (event.touches.length === 2) pinchDistance = touchesDistance(event.touches);
});

renderer.canvas.addEventListener(
  "touchmove",
  (event) => {
    if (event.touches.length !== 2 || pinchDistance === null) return;
    event.preventDefault();
    const distance = touchesDistance(event.touches);
    renderer.zoomBy(distance / pinchDistance);
    pinchDistance = distance;
  },
  { passive: false },
);

renderer.canvas.addEventListener("touchend", () => {
  pinchDistance = null;
});
