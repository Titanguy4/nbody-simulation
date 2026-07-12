# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A real-time N-body gravity simulation. The Spring Boot backend (`api/`) integrates Newtonian physics
at a fixed tick rate and publishes universe state over MQTT; a static "dumb client" (`client/index.html`)
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
sync with the hardcoded topic constants at the top of `client/index.html`'s `<script>` block (e.g.
`MQTT_TOPIC_ADD`, `MQTT_TOPIC_PRESET`). Note the client currently publishes clears to
`simulation/event/clear`, which has no corresponding adapter/channel in `MqttConfig` — check this wiring
before assuming a topic is handled.

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

`client/index.html` is a single self-contained file (markup, CSS, and JS inline) — no bundler, no
package.json. It uses `mqtt.js` (loaded from CDN, not vendored) to connect directly to the Mosquitto
WebSocket listener and both publishes user actions (click-to-add body, preset buttons, pause toggle,
clear) and subscribes to the `simulation` topic to redraw the canvas on every published tick.
