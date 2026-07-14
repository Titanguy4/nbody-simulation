// Lucide icons: only the ones referenced by `data-lucide` attributes in the
// DOM are registered, so the rest of the icon set is tree-shaken away.
// Call refreshIcons() after injecting new `data-lucide` markup.
import {
  Bomb,
  createIcons,
  MousePointerClick,
  Pause,
  Play,
  Sparkles,
  Wifi,
  WifiOff,
} from "lucide";

export function refreshIcons(): void {
  createIcons({
    icons: { Bomb, MousePointerClick, Pause, Play, Sparkles, Wifi, WifiOff },
  });
}
