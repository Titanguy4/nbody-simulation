// Renderer: draws simulation ticks onto the canvas. View math lives in
// Viewport, the launch arrow in aim-overlay. Knows nothing about MQTT or
// the side panel.
import { drawAim, type Aim } from "./aim-overlay";
import { COLORS } from "./config";
import { bodyTypeName, type Body, type Vec2 } from "./types";
import { Viewport } from "./viewport";

interface RenderedBody {
  body: Body;
  screenX: number;
  screenY: number;
  radius: number;
}

export class Renderer {
  readonly canvas: HTMLCanvasElement;
  readonly viewport: Viewport;

  private readonly ctx: CanvasRenderingContext2D;
  private renderedBodies: RenderedBody[] = [];
  private lastBodies: Body[] = [];
  private aim: Aim | null = null;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d")!;
    this.viewport = new Viewport(canvas);

    window.addEventListener("resize", () => this.resize());
    this.resize();
  }

  resize(): void {
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
    this.viewport.rescale();
  }

  zoom(deltaY: number, pivotX?: number, pivotY?: number): void {
    this.zoomBy(deltaY < 0 ? 1.25 : 0.8, pivotX, pivotY);
  }

  zoomBy(factor: number, pivotX?: number, pivotY?: number): void {
    this.viewport.zoomBy(factor, pivotX, pivotY);
    this.clear();
  }

  resetView(): void {
    this.viewport.reset();
  }

  screenToWorld(clientX: number, clientY: number): Vec2 {
    return this.viewport.screenToWorld(clientX, clientY);
  }

  clear(): void {
    this.ctx.fillStyle = "#050510";
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
  }

  private drawScale(): void {
    const targetMeters = 150 / this.viewport.scale;
    const exponent = Math.floor(Math.log10(targetMeters));
    const fraction = targetMeters / Math.pow(10, exponent);
    const niceFraction = fraction >= 5 ? 5 : fraction >= 2 ? 2 : 1;

    const niceMeters = niceFraction * Math.pow(10, exponent);
    const barWidth = niceMeters * this.viewport.scale;

    let text = "";
    const km = niceMeters / 1000;
    if (km >= 1e9) text = (km / 1e9).toFixed(0) + " milliards km";
    else if (km >= 1e6) text = (km / 1e6).toFixed(0) + " millions km";
    else text = km.toLocaleString() + " km";

    const au = niceMeters / 1.496e11;
    if (au >= 0.1)
      text += `  (~${au >= 10 ? au.toFixed(0) : au.toFixed(2)} UA)`;

    const x = 20,
      y = this.canvas.height - 30;
    const ctx = this.ctx;
    ctx.strokeStyle = "rgba(255, 255, 255, 0.7)";
    ctx.fillStyle = "rgba(255, 255, 255, 0.7)";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + barWidth, y);
    ctx.moveTo(x, y - 5);
    ctx.lineTo(x, y + 5);
    ctx.moveTo(x + barWidth, y - 5);
    ctx.lineTo(x + barWidth, y + 5);
    ctx.stroke();

    ctx.font = "12px sans-serif";
    ctx.shadowBlur = 0;
    ctx.fillText(text, x, y - 10);
  }

  drawUniverse(bodies: Body[]): void {
    this.lastBodies = bodies;
    const ctx = this.ctx;
    // While aiming, repaint fully opaque so the previous arrow frame does
    // not ghost; the motion-trail effect resumes on release.
    ctx.fillStyle = this.aim ? "#050510" : "rgba(5, 5, 16, 0.3)";
    ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    this.renderedBodies = [];

    bodies.forEach((body) => {
      const { x: screenX, y: screenY } = this.viewport.worldToScreen(
        body.position.x,
        body.position.y,
      );

      if (
        screenX < 0 ||
        screenX > this.canvas.width ||
        screenY < 0 ||
        screenY > this.canvas.height
      )
        return;

      const bodyName = bodyTypeName(body);
      const isStar = bodyName === "Sun" || bodyName === "STAR";
      const radius = isStar ? 10 : 5;

      ctx.beginPath();
      ctx.arc(screenX, screenY, radius, 0, 2 * Math.PI);
      ctx.fillStyle = COLORS[bodyName] || "#ffffff";
      ctx.shadowBlur = isStar ? 5 : 2;
      ctx.shadowColor = ctx.fillStyle;
      ctx.fill();

      this.renderedBodies.push({ body, screenX, screenY, radius });
    });
    this.drawScale();
    if (this.aim) drawAim(ctx, this.aim);
  }

  // Aim overlay shown while the user drags to launch a body.
  setAim(aim: Aim): void {
    this.aim = aim;
    this.redraw();
  }

  clearAim(): void {
    this.aim = null;
    this.redraw();
  }

  redraw(): void {
    this.drawUniverse(this.lastBodies);
  }

  // Finds the topmost body under (clientX, clientY), with a little extra
  // hit-testing margin so small bodies stay easy to hover.
  getBodyAt(clientX: number, clientY: number): Body | null {
    const rect = this.canvas.getBoundingClientRect();
    const x = clientX - rect.left;
    const y = clientY - rect.top;

    for (let i = this.renderedBodies.length - 1; i >= 0; i--) {
      const { body, screenX, screenY, radius } = this.renderedBodies[i];
      const hitRadius = Math.max(radius, 6) + 3;
      const dx = x - screenX;
      const dy = y - screenY;
      if (dx * dx + dy * dy <= hitRadius * hitRadius) return body;
    }
    return null;
  }
}
