// Shared domain types for the simulation client.

export interface Vec2 {
  x: number;
  y: number;
}

// Body as exchanged with the backend over MQTT. `type` is an object when
// serialized by the API but may degrade to a plain string.
export interface Body {
  id: number;
  type: { name: string } | string;
  mass: number;
  position: Vec2;
  velocity: Vec2;
  acceleration: Vec2;
}

export function bodyTypeName(body: Body): string {
  return typeof body.type === "string" ? body.type : body.type.name;
}
