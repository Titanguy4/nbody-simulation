// Panel: owns the side-panel DOM (status, info, form, buttons). Exposes
// on*/get*/set* methods instead of raw DOM nodes, so callers never reach
// into its internals directly.
class Panel {
  constructor() {
    this.statusDiv = document.getElementById("status");
    this.infoDiv = document.getElementById("info");
    this.toggleBtn = document.getElementById("btn-toggle-sim");
    this.clearBtn = document.getElementById("btn-clear");
    this.presetBtns = document.querySelectorAll(".preset-btn[data-preset]");
    this.tooltip = document.getElementById("body-tooltip");
    this.createPanel = document.getElementById("create-panel");

    // On small screens the create panel starts collapsed: opened, it
    // would cover half the canvas.
    if (window.matchMedia("(max-width: 600px)").matches)
      this.createPanel.removeAttribute("open");
  }

  setConnected() {
    this.statusDiv.innerHTML =
      '<i data-lucide="wifi" class="icon"></i> Connecté en direct';
    this.statusDiv.className = "connected";
    lucide.createIcons();
  }

  setBodyCount(count) {
    this.infoDiv.innerText = count + " corps célestes en orbite";
  }

  setPlaying(isPlaying) {
    if (isPlaying) {
      this.toggleBtn.title = "Mettre en Pause";
      this.toggleBtn.innerHTML = '<i data-lucide="pause" class="icon"></i>';
    } else {
      this.toggleBtn.title = "Reprendre";
      this.toggleBtn.innerHTML = '<i data-lucide="play" class="icon"></i>';
    }
    lucide.createIcons();
  }

  getFormValues() {
    return {
      type: document.getElementById("input-type").value,
      mass: Number.parseFloat(document.getElementById("input-mass").value),
      vx: Number.parseFloat(document.getElementById("input-vx").value),
      vy: Number.parseFloat(document.getElementById("input-vy").value),
    };
  }

  showTooltip(body, clientX, clientY) {
    const bodyName = body.type.name || body.type;
    const speed = Math.hypot(body.velocity.x, body.velocity.y);

    this.tooltip.innerHTML = `
      <div class="tooltip-title">${bodyName}</div>
      <div class="tooltip-row"><span>ID</span><span>${body.id}</span></div>
      <div class="tooltip-row"><span>Masse</span><span>${formatMass(body.mass)}</span></div>
      <div class="tooltip-row"><span>Position X</span><span>${formatDistance(body.position.x)}</span></div>
      <div class="tooltip-row"><span>Position Y</span><span>${formatDistance(body.position.y)}</span></div>
      <div class="tooltip-row"><span>Vitesse</span><span>${formatSpeed(speed)}</span></div>
      <div class="tooltip-row"><span>Vitesse X</span><span>${formatSpeed(body.velocity.x)}</span></div>
      <div class="tooltip-row"><span>Vitesse Y</span><span>${formatSpeed(body.velocity.y)}</span></div>
    `;
    this.tooltip.classList.remove("hidden");

    const offset = 14;
    const maxLeft = window.innerWidth - this.tooltip.offsetWidth - 8;
    const maxTop = window.innerHeight - this.tooltip.offsetHeight - 8;
    this.tooltip.style.left = Math.min(clientX + offset, maxLeft) + "px";
    this.tooltip.style.top = Math.min(clientY + offset, maxTop) + "px";
  }

  hideTooltip() {
    this.tooltip.classList.add("hidden");
  }

  onToggle(callback) {
    this.toggleBtn.addEventListener("click", callback);
  }

  onClear(callback) {
    this.clearBtn.addEventListener("click", callback);
  }

  onPresetSelect(callback) {
    this.presetBtns.forEach((btn) => {
      btn.addEventListener("click", () =>
        callback(btn.getAttribute("data-preset")),
      );
    });
  }
}
