// Canvas interactions: a plain click drops a static body, press-and-drag
// aims a launch arrow (direction = velocity vector, length = magnitude),
// hover/tap shows a body's info, wheel and pinch zoom toward the pointer.
import type { MqttClient } from "./mqtt-client";
import type { Panel } from "./panel";
import type { Renderer } from "./renderer";

const SPEED_PER_PIXEL = 150; // m/s of velocity per dragged pixel
const DRAG_THRESHOLD = 6; // px of movement before a press becomes an aim

export function bindInteractions(
  mqttClient: MqttClient,
  renderer: Renderer,
  panel: Panel,
): void {
  const canvas = renderer.canvas;
  let press: { startX: number; startY: number; aiming: boolean } | null = null;

  function spawnBody(
    clientX: number,
    clientY: number,
    vx: number,
    vy: number,
  ): void {
    const position = renderer.screenToWorld(clientX, clientY);
    const form = panel.getFormValues();
    mqttClient.addBody({
      id: Math.floor(Math.random() * 1000000),
      type: { name: form.type },
      mass: form.mass,
      position,
      velocity: { x: vx, y: vy },
      acceleration: { x: 0.0, y: 0.0 },
    });
  }

  window.addEventListener("wheel", (event) =>
    renderer.zoom(event.deltaY, event.clientX, event.clientY),
  );

  canvas.addEventListener("mousemove", (event) => {
    if (press) return;
    const hovered = renderer.getBodyAt(event.clientX, event.clientY);
    if (hovered) panel.showTooltip(hovered, event.clientX, event.clientY);
    else panel.hideTooltip();
  });

  canvas.addEventListener("mouseleave", () => panel.hideTooltip());

  canvas.addEventListener("pointerdown", (event) => {
    if (!event.isPrimary) return;
    press = { startX: event.clientX, startY: event.clientY, aiming: false };
    canvas.style.cursor = "none";
    canvas.setPointerCapture(event.pointerId);
  });

  canvas.addEventListener("pointermove", (event) => {
    if (!press || !event.isPrimary) return;
    const dx = event.clientX - press.startX;
    const dy = event.clientY - press.startY;
    if (!press.aiming && Math.hypot(dx, dy) < DRAG_THRESHOLD) return;
    press.aiming = true;
    panel.hideTooltip();
    renderer.setAim({
      startX: press.startX,
      startY: press.startY,
      endX: event.clientX,
      endY: event.clientY,
      speed: Math.hypot(dx, dy) * SPEED_PER_PIXEL,
    });
  });

  canvas.addEventListener("pointerup", (event) => {
    if (!press || !event.isPrimary) return;
    const { startX, startY, aiming } = press;
    press = null;
    canvas.style.cursor = "";

    if (!aiming) {
      // Plain click/tap: show a body's info when one is under the pointer
      // (only tooltip access on touch screens), otherwise add a static body.
      const tapped = renderer.getBodyAt(startX, startY);
      if (tapped) {
        panel.showTooltip(tapped, startX, startY);
        return;
      }
      panel.hideTooltip();
      spawnBody(startX, startY, 0, 0);
      return;
    }

    renderer.clearAim();
    spawnBody(
      startX,
      startY,
      (event.clientX - startX) * SPEED_PER_PIXEL,
      (event.clientY - startY) * SPEED_PER_PIXEL,
    );
  });

  canvas.addEventListener("pointercancel", () => {
    press = null;
    canvas.style.cursor = "";
    renderer.clearAim();
  });

  // Pinch-to-zoom on touch screens, the counterpart of the wheel listener.
  let pinchDistance: number | null = null;
  const touchesDistance = (touches: TouchList) =>
    Math.hypot(
      touches[0].clientX - touches[1].clientX,
      touches[0].clientY - touches[1].clientY,
    );

  canvas.addEventListener("touchstart", (event) => {
    if (event.touches.length !== 2) return;
    // A second finger switches from aiming to pinch-zoom.
    press = null;
    renderer.clearAim();
    pinchDistance = touchesDistance(event.touches);
  });

  canvas.addEventListener(
    "touchmove",
    (event) => {
      if (event.touches.length !== 2 || pinchDistance === null) return;
      event.preventDefault();
      const distance = touchesDistance(event.touches);
      renderer.zoomBy(
        distance / pinchDistance,
        (event.touches[0].clientX + event.touches[1].clientX) / 2,
        (event.touches[0].clientY + event.touches[1].clientY) / 2,
      );
      pinchDistance = distance;
    },
    { passive: false },
  );

  canvas.addEventListener("touchend", () => {
    pinchDistance = null;
  });
}
