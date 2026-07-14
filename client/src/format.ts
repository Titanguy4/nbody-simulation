// Human-readable formatting of the physical quantities shown in the UI.

export function formatMass(kg: number): string {
  return kg.toExponential(2).replace("e+", " × 10^") + " kg";
}

export function formatDistance(meters: number): string {
  const au = meters / 1.496e11;
  if (Math.abs(au) >= 0.01) return au.toFixed(3) + " UA";
  return (meters / 1000).toFixed(0) + " km";
}

export function formatSpeed(metersPerSecond: number): string {
  return metersPerSecond.toFixed(0) + " m/s";
}
