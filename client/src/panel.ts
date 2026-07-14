// Panel: owns the side-panel DOM (status, info, form, buttons). Exposes
// on*/get*/set* methods instead of raw DOM nodes, so callers never reach
// into its internals directly.
import { formatDistance, formatMass, formatSpeed } from "./format";
import { refreshIcons } from "./icons";
import { bodyTypeName, type Body } from "./types";

export interface FormValues {
  type: string;
  mass: number;
}

export class Panel {
  private readonly statusDiv = document.getElementById("status")!;
  private readonly infoDiv = document.getElementById("info")!;
  private readonly toggleBtn = document.getElementById("btn-toggle-sim")!;
  private readonly clearBtn = document.getElementById("btn-clear")!;
  private readonly presetBtns = document.querySelectorAll(
    ".preset-btn[data-preset]",
  );
  private readonly tooltip = document.getElementById("body-tooltip")!;
  private readonly createPanel = document.getElementById("create-panel")!;

  constructor() {
    // On small screens the create panel starts collapsed: opened, it
    // would cover half the canvas.
    if (window.matchMedia("(max-width: 600px)").matches)
      this.createPanel.removeAttribute("open");
  }

  setConnected(): void {
    this.statusDiv.innerHTML =
      '<i data-lucide="wifi" class="icon"></i> Connecté en direct';
    this.statusDiv.className = "connected";
    refreshIcons();
  }

  setBodyCount(count: number): void {
    this.infoDiv.innerText = count + " corps célestes en orbite";
  }

  setPlaying(isPlaying: boolean): void {
    if (isPlaying) {
      this.toggleBtn.title = "Mettre en Pause";
      this.toggleBtn.innerHTML = '<i data-lucide="pause" class="icon"></i>';
    } else {
      this.toggleBtn.title = "Reprendre";
      this.toggleBtn.innerHTML = '<i data-lucide="play" class="icon"></i>';
    }
    refreshIcons();
  }

  getFormValues(): FormValues {
    return {
      type: (document.getElementById("input-type") as HTMLSelectElement).value,
      mass: Number.parseFloat(
        (document.getElementById("input-mass") as HTMLInputElement).value,
      ),
    };
  }

  showTooltip(body: Body, clientX: number, clientY: number): void {
    const speed = Math.hypot(body.velocity.x, body.velocity.y);

    this.tooltip.innerHTML = `
      <div class="tooltip-title">${bodyTypeName(body)}</div>
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

  hideTooltip(): void {
    this.tooltip.classList.add("hidden");
  }

  onToggle(callback: () => void): void {
    this.toggleBtn.addEventListener("click", callback);
  }

  onClear(callback: () => void): void {
    this.clearBtn.addEventListener("click", callback);
  }

  onPresetSelect(callback: (presetName: string) => void): void {
    this.presetBtns.forEach((btn) => {
      btn.addEventListener("click", () =>
        callback(btn.getAttribute("data-preset")!),
      );
    });
  }
}
