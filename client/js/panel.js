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
