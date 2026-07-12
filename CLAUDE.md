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

Frontend: open `client/index.html` directly in a browser (no build step, no dependencies to install).

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
sync with the topic constants in `client/js/config.js` (e.g. `MQTT_TOPIC_ADD`, `MQTT_TOPIC_PRESET`). Note
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

`client/` is a bundler-free, dependency-free static site split by responsibility (not MVC — there's no
persistent client-side domain model to justify it; state lives server-side and each MQTT tick is drawn
straight through) and wired together with plain (non-module) `<script>` tags, in load order, from
`client/index.html`:

- `client/css/style.css` — all styling, extracted from the former inline `<style>` block.
- `client/js/config.js` — MQTT topic/broker constants and body-type color map.
- `client/js/mqtt-client.js` — **`MqttClient`**, the transport layer. Owns the `mqtt.js` connection
  (loaded from CDN, not vendored) and play/pause state; exposes `addBody`/`clearUniverse`/`loadPreset`/
  `togglePause` and `onConnect`/`onBodiesUpdate` callbacks. No DOM or canvas access.
- `client/js/renderer.js` — **`Renderer`**, canvas rendering. Scale/zoom math, world↔screen conversion,
  drawing bodies and the distance-scale bar.
- `client/js/panel.js` — **`Panel`**, the side-panel DOM. Exposes `on*`/`get*`/`set*` methods
  (`onToggle`, `onClear`, `onPresetSelect`, `getFormValues`, `setConnected`, `setBodyCount`,
  `setPlaying`) rather than raw DOM nodes, so callers never reach into its internals.
- `client/js/main.js` — wiring only: instantiates `MqttClient`/`Renderer`/`Panel` and connects their
  callbacks/events to each other. This is the only file that knows about all three.

Scripts are intentionally not ES modules: `client/index.html` is opened directly via `file://` (no
build step, no local server required), and browsers block `type="module"` script loading under that
scheme. Top-level `const`/`class` declarations in each classic script share the page's global scope, so
later scripts (e.g. `main.js`) can reference earlier ones (e.g. `MqttClient`) without imports — keep the
`<script>` load order in `index.html` (config → mqtt-client → renderer → panel → main) when adding files.
