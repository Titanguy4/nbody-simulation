// Renderer: draws simulation ticks onto the canvas. Knows nothing about
// MQTT or the side panel.
class Renderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.scale = 1;
    this.universeRadius = 8e11;

    window.addEventListener("resize", () => this.resize());
    this.resize();
  }

  resize() {
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
    this.calculateScale();
  }

  calculateScale() {
    this.scale =
      Math.min(this.canvas.width, this.canvas.height) /
      (2 * this.universeRadius);
  }

  zoom(deltaY) {
    if (deltaY < 0) this.universeRadius *= 0.8;
    else this.universeRadius *= 1.25;
    this.calculateScale();
    this.clear();
  }

  clear() {
    this.ctx.fillStyle = "#050510";
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
  }

  screenToWorld(clientX, clientY) {
    const centerX = this.canvas.width / 2;
    const centerY = this.canvas.height / 2;
    return {
      x: (clientX - centerX) / this.scale,
      y: (clientY - centerY) / this.scale,
    };
  }

  drawScale() {
    const targetMeters = 150 / this.scale;
    const exponent = Math.floor(Math.log10(targetMeters));
    const fraction = targetMeters / Math.pow(10, exponent);
    let niceFraction = fraction >= 5 ? 5 : fraction >= 2 ? 2 : 1;

    const niceMeters = niceFraction * Math.pow(10, exponent);
    const barWidth = niceMeters * this.scale;

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

  drawUniverse(bodies) {
    const ctx = this.ctx;
    ctx.fillStyle = "rgba(5, 5, 16, 0.3)";
    ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    const centerX = this.canvas.width / 2;
    const centerY = this.canvas.height / 2;

    bodies.forEach((body) => {
      const screenX = centerX + body.position.x * this.scale;
      const screenY = centerY + body.position.y * this.scale;

      if (
        screenX < 0 ||
        screenX > this.canvas.width ||
        screenY < 0 ||
        screenY > this.canvas.height
      )
        return;

      const bodyName = body.type.name || body.type;
      const isStar = bodyName === "Sun" || bodyName === "STAR";
      const radius = isStar ? 6 : 2.5;

      ctx.beginPath();
      ctx.arc(screenX, screenY, radius, 0, 2 * Math.PI);
      ctx.fillStyle = COLORS[bodyName] || "#ffffff";
      ctx.shadowBlur = isStar ? 15 : 0;
      ctx.shadowColor = ctx.fillStyle;
      ctx.fill();
    });
    this.drawScale();
  }
}
