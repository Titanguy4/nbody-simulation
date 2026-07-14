# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A real-time N-body gravity simulation. The Spring Boot backend (`api/`) integrates Newtonian physics
at a fixed tick rate and publishes universe state over MQTT; a static "dumb client" (`client/`)
subscribes over MQTT-over-WebSockets and renders bodies to an HTML5 canvas. There is no HTTP API between
client and server — all communication is MQTT pub/sub, brokered by Mosquitto.

```
Frontend JS (client/index.html) <--WebSockets:9001--> Mosquitto <--MQTT TCP:1883--> Spring Boot (api/)
```

## Commands

All backend commands run from `api/`.

- Run the backend: `./gradlew bootRun` (from `api/`)
- Run with verbose MQTT topic logging: `./gradlew bootRun --args='--spring.profiles.active=DEBUG'`
- Build: `./gradlew build`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.titanguy.nbody.SomeTest"`

Frontend (from `client/`, requires [bun](https://bun.sh)):

- Install dev dependencies (TypeScript + Vite): `bun install`
- Dev server with hot reload: `bun run dev` (serves on the Vite default port)
- Production build (type-check via `tsc --noEmit`, then Vite bundle to `dist/`): `bun run build`
- Type-check only: `bun run typecheck`; preview the built bundle: `bun run preview`

Broker: `docker-compose up` from `api/` starts Mosquitto with WebSocket support enabled (ports 1883 and
9001), using `api/mosquitto/config/mosquitto.conf`.

Requires Java 25 and Gradle 9.6.1 (see `api/build.gradle.kts`, targets Spring Boot 4.1.0).

## Architecture

### MQTT is the entire integration layer

There are no REST controllers. `MqttConfig` (`api/src/main/java/com/titanguy/nbody/configs/MqttConfig.java`)
wires everything through Spring Integration `MessageChannel`s: an `Mqttv5PahoMessageDrivenChannelAdapter`
per incoming topic, a `JsonToObjectTransformer` that deserializes payloads into `BodyDto`, and a single
outbound `Mqttv5PahoMessageHandler` for publishing simulation snapshots. `@ServiceActivator`-annotated
methods (in `NBodyController` and `SimulationService`) are the actual message handlers — there is no
dispatch code to trace, Spring Integration wires channel name (a string) to method via the annotation.
When adding a new inbound event type, the pattern is: new topic property in `application.yml` → new
`DirectChannel` bean + adapter in `MqttConfig` → new `@ServiceActivator(inputChannel = "...")` method.

Topics are configured in `api/src/main/resources/application.yml` under `mqtt.topic.*` and must stay in
sync with the topic constants in `client/src/config.ts` (e.g. `MQTT_TOPIC_ADD`, `MQTT_TOPIC_PRESET`). Note
the client currently publishes clears to `simulation/event/clear`, which has no corresponding
adapter/channel in `MqttConfig` — check this wiring before assuming a topic is handled.

### Simulation loop

`SimulationService` owns the physics loop: a single-thread `ThreadPoolTaskScheduler` ticks every
`UPDATE_INTERVAL_MS` (10ms wall-clock, advancing 12 virtual hours per tick), doing an O(n²) pairwise
force calculation with Plummer softening (`eps`) to avoid singularities on close approach, then
publishes the full body list as JSON to the outbound MQTT channel. The loop can be stopped/started via
the `pauseEventChannel` (`PLAY`/`STOP` string payloads on `simulation/event/pause`), driven by the
client's play/pause button.

`NBodyService` holds the live `List<Body>` — the single in-memory source of truth for simulation state
(no persistence). `Body.equals`/`hashCode` are overridden to key only on `id` (see `Body.java`), which
`NBodyService.updateBody` relies on to do `indexOf`-based replacement.

`Presets` (`api/src/main/java/com/titanguy/nbody/configs/Presets.java`) procedurally builds scenario
starting states (solar system, binary system, galaxy cluster/collision, supernova, planetary ring) by
mutating `NBodyService`'s body list directly. Triggered by the `simulation/preset` topic. On startup,
`SimulationService.init()` (`@PostConstruct`) loads the solar system preset and starts the loop after a
2-second delay.

### Validation and error handling

`BodyDto` (record) carries Jakarta Bean Validation annotations; `NBodyController` methods are
`@Validated @Payload @Valid`. Invalid/malformed MQTT payloads never throw synchronously — they route
through the channel adapter's `errorChannel` to `MqttErrorHandler`, which unwraps the exception chain
(`ConstraintViolationException` vs `MessageConversionException`/`IllegalArgumentException`) and logs a
warning rather than crashing the flow.

### Frontend

`client/` is a TypeScript + Vite static site with no runtime dependencies: `index.html` references
`src/main.ts` directly and Vite serves it in dev (`bun run dev`, with HMR) or bundles it for prod
(`bun run build` → `client/dist/`, gitignored generated output — never edit it by hand). TypeScript
is strict-mode, type-checked by `tsc --noEmit` as part of the build. `mqtt` and `lucide` are regular
npm dependencies bundled by Vite (no CDN); lucide icons are registered one by one in `src/icons.ts`
so unused icons tree-shake away — add new icons there when adding `data-lucide` markup.

Modules under `client/src/`, wired by explicit extensionless imports (`moduleResolution: bundler`):

- `types.ts` — shared domain types (`Body`, `Vec2`) and the `bodyTypeName` helper (the backend
  serializes `type` as an object but it may degrade to a plain string).
- `config.ts` — MQTT topic/broker constants and body-type color map. Topics must stay in sync with
  `application.yml` on the API side.
- `format.ts` — display formatting for masses, distances and speeds.
- `icons.ts` — registers the lucide icons used by the UI (`refreshIcons()`, called after injecting
  `data-lucide` markup).
- `mqtt-client.ts` — **`MqttClient`**, the transport layer. Owns the `mqtt.js` connection and
  play/pause state; exposes `addBody`/`clearUniverse`/`loadPreset`/`togglePause` and
  `onConnect`/`onBodiesUpdate` callbacks. No DOM or canvas access.
- `viewport.ts` — **`Viewport`**, pure view math: zoom level, view center (moves when zooming toward
  a pivot so the point under the cursor stays put) and world↔screen conversions. No drawing.
- `aim-overlay.ts` — draws the launch arrow shown while press-dragging a new body.
- `renderer.ts` — **`Renderer`**, canvas drawing (bodies, distance-scale bar, aim overlay); owns the
  `Viewport` (exposed as `renderer.viewport`) and keeps the last drawn frame for redraws.
- `panel.ts` — **`Panel`**, the side-panel DOM. Exposes `on*`/`get*`/`set*` methods rather than raw
  DOM nodes, so callers never reach into its internals.
- `interactions.ts` — all canvas input: plain click adds a static body, press-and-drag aims a launch
  arrow (direction = velocity vector, length = magnitude, 150 m/s per pixel), click/tap on a body
  shows its tooltip, wheel and two-finger pinch zoom toward the pointer.
- `main.ts` — wiring only: instantiates the components and connects their callbacks. This is the only
  file that knows about all of them.

The client Docker image builds in two stages: `oven/bun` runs the Vite build, then `nginx:alpine`
serves the resulting `dist/`.
