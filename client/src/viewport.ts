// Viewport: pure view math — zoom level, view center and the
// world <-> screen coordinate conversions. No drawing.
import type { Vec2 } from "./types";

export class Viewport {
  scale = 1;
  universeRadius = 8e11;
  // World coordinates displayed at the center of the screen; moves when
  // zooming toward a point so that point stays put.
  viewCenter: Vec2 = { x: 0, y: 0 };

  constructor(private readonly canvas: HTMLCanvasElement) {}

  rescale(): void {
    this.scale =
      Math.min(this.canvas.width, this.canvas.height) /
      (2 * this.universeRadius);
  }

  // factor > 1 zooms in, < 1 zooms out. When a pivot (screen coords) is
  // given, the world point under it stays under it — zoom toward the
  // cursor or the pinch midpoint.
  zoomBy(factor: number, pivotX?: number, pivotY?: number): void {
    const anchor =
      pivotX !== undefined && pivotY !== undefined
        ? this.screenToWorld(pivotX, pivotY)
        : null;
    this.universeRadius /= factor;
    this.rescale();
    if (anchor && pivotX !== undefined && pivotY !== undefined) {
      const drifted = this.screenToWorld(pivotX, pivotY);
      this.viewCenter.x += anchor.x - drifted.x;
      this.viewCenter.y += anchor.y - drifted.y;
    }
  }

  reset(): void {
    this.viewCenter = { x: 0, y: 0 };
  }

  screenToWorld(clientX: number, clientY: number): Vec2 {
    return {
      x: this.viewCenter.x + (clientX - this.canvas.width / 2) / this.scale,
      y: this.viewCenter.y + (clientY - this.canvas.height / 2) / this.scale,
    };
  }

  worldToScreen(worldX: number, worldY: number): Vec2 {
    return {
      x: this.canvas.width / 2 + (worldX - this.viewCenter.x) * this.scale,
      y: this.canvas.height / 2 + (worldY - this.viewCenter.y) * this.scale,
    };
  }
}
