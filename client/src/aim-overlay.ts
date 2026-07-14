// Aim overlay: the thin launch arrow drawn while the user drags to launch
// a body. Screen coordinates.

export interface Aim {
  startX: number;
  startY: number;
  endX: number;
  endY: number;
  speed: number; // m/s
}

export function drawAim(ctx: CanvasRenderingContext2D, aim: Aim): void {
  const { startX, startY, endX, endY, speed } = aim;

  ctx.save();
  ctx.shadowBlur = 0;
  ctx.strokeStyle = "rgba(244, 244, 245, 0.9)";
  ctx.fillStyle = "rgba(244, 244, 245, 0.9)";
  ctx.lineWidth = 1.5;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";

  // Anchor dot at the future body position.
  ctx.beginPath();
  ctx.arc(startX, startY, 3, 0, 2 * Math.PI);
  ctx.fill();

  // Straight shaft: the arrow points exactly where the body will go.
  ctx.beginPath();
  ctx.moveTo(startX, startY);
  ctx.lineTo(endX, endY);
  ctx.stroke();

  // Chevron head aligned with the drag direction.
  const angle = Math.atan2(endY - startY, endX - startX);
  const head = 8;
  ctx.beginPath();
  ctx.moveTo(
    endX - head * Math.cos(angle - Math.PI / 5),
    endY - head * Math.sin(angle - Math.PI / 5),
  );
  ctx.lineTo(endX, endY);
  ctx.lineTo(
    endX - head * Math.cos(angle + Math.PI / 5),
    endY - head * Math.sin(angle + Math.PI / 5),
  );
  ctx.stroke();

  // Speed label just past the tip.
  const label =
    speed >= 1000
      ? (speed / 1000).toFixed(1) + " km/s"
      : Math.round(speed) + " m/s";
  ctx.font = "11px 'Segoe UI', sans-serif";
  ctx.fillStyle = "rgba(244, 244, 245, 0.75)";
  ctx.textAlign = Math.cos(angle) >= 0 ? "left" : "right";
  ctx.textBaseline = "middle";
  ctx.fillText(label, endX + 14 * Math.cos(angle), endY + 14 * Math.sin(angle));
  ctx.restore();
}
