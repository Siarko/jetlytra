# Jetlytra Mod — Architecture

## Context

A standalone NeoForge 1.21.1 mod implementing a jetpack item with dual flight modes. Lives alongside Controlify and controlifycreate in the same workspace. Includes Controlify gamepad support from day one.

- **Jetpack mode**: thrust up with Jump, hover with Crouch, normal WASD movement
- **Elytra mode**: activated by Sprint while airborne — uses vanilla elytra physics with wings fully spread
- A 3D animated model (GeckoLib) where wings expand/fold on mode transitions

---

## Project Setup

Mirrors controlifycreate exactly, with additions for GeckoLib and Controlify.

```
/workspace/jetlytra/
  settings.gradle
  build.gradle             # NeoForge moddev + GeckoLib + Controlify deps
  gradle.properties
    mod_id=jetlytra
    mod_name=Jetlytra
    mod_group_id=pl.siarko.jetlytra
    mod_version=1.0.0
    minecraft_version=1.21.1
    neo_version=21.1.219
    parchment_minecraft_version=1.21.1
    parchment_mappings_version=2024.11.17
    geckolib_version=4.7.6
```

---

## Package Structure

```
src/main/java/pl/siarko/jetlytra/
  Jetlytra.java                           # @Mod entry point
  JetlytraClient.java                     # @Mod(dist=CLIENT) client init

  item/
    JetlytraItem.java                     # ArmorItem + GeoArmorItem
    JetlytraItems.java                    # DeferredRegister<Item>
    JetlytraCreativeTab.java              # DeferredRegister<CreativeModeTab>

  capability/
    JetpackCapabilityImpl.java            # Per-player state: FlightState + fuel + thrustActive
    JetpackCapabilityAttacher.java        # AttachCapabilitiesEvent + PlayerEvent.Clone

  flight/
    FlightState.java                      # Enum: OFF, JETPACK, HOVERING, ELYTRA
    FlightStateManager.java               # Transition logic / validation
    JetpackPhysicsHandler.java            # PlayerTickEvent.Post — server physics tick
    HoverController.java                  # Altitude-hold math (cancel gravity)
    FuelSystem.java                       # Durability-based fuel drain

  network/
    JetpackPackets.java                   # RegisterPayloadsEvent — all packet registration
    C2SToggleJetpackPacket.java           # Toggle ON<->OFF
    C2SThrustPacket.java                  # Jump pressed/released
    C2SCrouchPacket.java                  # Crouch (JETPACK->HOVER or ELYTRA->JETPACK)
    C2SSprintElytraPacket.java            # Sprint while airborne (JETPACK->ELYTRA)
    S2CSyncStatePacket.java               # Broadcast state+fuel to nearby clients

  controlify/
    JetlytraControlifyEntrypoint.java     # ControlifyEntrypoint: register gamepad binds

  mixin/
    LivingEntityMixin.java                # Override isFallFlying() for ELYTRA state
    PlayerMixin.java                      # Detect landing (ground -> transition to OFF)

  client/
    input/
      JetpackKeyMappings.java             # Toggle keybind (default R)
      JetpackInputHandler.java            # ClientTickEvent.Pre: read keys, send packets
    particle/
      JetpackParticleHandler.java         # ClientTickEvent.Pre: spawn flame+smoke particles
    render/
      JetpackGeoModel.java                # GeckoLib GeoModel (model/texture/anim paths)
      JetpackArmorRenderer.java           # GeckoLib GeoArmorRenderer subclass
      JetpackAnimationController.java     # State->animation mapping
    hud/
      JetpackHudOverlay.java              # RegisterGuiLayersEvent: fuel bar + mode label
```

---

## State Machine

```
                      OFF
                       |  Toggle keybind (item equipped, fuel > 0)
                       v
                   JETPACK <------------------------+
                   |     |                          |
       Shift       |     |  Sprint (while airborne) |  Shift
     (just-press)  |     |                          |  (just-press)
                   v     v                          |
               HOVERING  ELYTRA -------------------+

  Any state -> OFF:
    - Toggle keybind again
    - Fuel reaches zero
    - Item removed from chest slot
    - Player touches water / mounts entity
    - Player touches ground (from ELYTRA)

  HOVERING -> JETPACK:
    - Shift released OR Jump pressed
```

Each `FlightState` enum value carries: `allowsThrust`, `cancelsGravity`, `usesFallFlying` flags for quick queries.

---

## Key Architecture Decisions

### Fuel: Item Durability
Simplest MVP — durability is the fuel gauge. Drain rate is a constant. Vanilla anvil repair works automatically. Zero extra data components needed. Refactorable to Forge Energy later.

### Elytra Physics: Mixin on `isFallFlying()`
`LivingEntityMixin` injects at HEAD of `isFallFlying()`. When state is ELYTRA → `cir.setReturnValue(true)`. This activates the entire vanilla elytra physics path in `LivingEntity#travel()` — air friction, directional glide, fall damage suppression — without duplicating any code.

### 3D Model: GeckoLib
Wing animation requires bone-level keyframe transitions — vanilla JSON models are static. GeckoLib is the established NeoForge standard. `JetlytraItem` implements `GeoArmorItem`. `JetpackArmorRenderer` extends `GeoArmorRenderer`. Blockbench `.bbmodel` → exported `.geo.json` + `.animation.json`.

### Physics: Server-Authoritative
All state transitions and force application happen server-side via `PlayerTickEvent.Post`. The client only sends intent packets (key pressed/released), never velocity or position. State synced back via `S2CSyncStatePacket`.

### Input: No Vanilla Key Suppression
`JetpackInputHandler` reads `options.keyJump`, `options.keyShift`, `options.keySprint` via `isDown()` and sends packets on edge changes only. Vanilla keys continue to work normally.

### Controlify: Parallel Bind Registration
`JetlytraControlifyEntrypoint` implements `ControlifyEntrypoint`, registered via `META-INF/services/`. Registers a controller bind for each action. `JetpackInputHandler` OR-s vanilla key state with Controlify bind state before sending packets.

---

## Physics Per State (Server Tick)

| State    | Physics Applied |
|----------|----------------|
| OFF      | Nothing — vanilla gravity |
| JETPACK  | If thrustActive: `dy += THRUST_ACCEL` (~0.1), capped at MAX_THRUST_VEL (~0.8). Gravity applies normally; thrust overcomes it. |
| HOVERING | `dy = +0.08` each tick (cancels 0.08 blocks/tick² gravity). XZ drag x0.85. |
| ELYTRA   | `setSharedFlag(7, true)` → vanilla elytra travel() takes over entirely. |

---

## Particle System

No custom particle type. Vanilla particles from `JetpackParticleHandler` (client-side):
- Position: player foot pos + 0.8Y, offset ±0.2X for dual nozzles
- `ParticleTypes.FLAME` — velocity `(±rand, -0.2, ±rand)`
- `ParticleTypes.LARGE_SMOKE` — velocity `(±rand, -0.15, ±rand)`
- Only spawned when state=JETPACK and thrustActive=true

---

## Animation States (GeckoLib)

| Animation        | Trigger                   | Wing Pose            |
|------------------|---------------------------|----------------------|
| `idle`           | OFF                       | Folded flat          |
| `jetpack_thrust` | JETPACK + thrustActive    | Half-spread, vibrate |
| `jetpack_glide`  | JETPACK, no thrust        | Half-spread, static  |
| `elytra_glide`   | ELYTRA                    | Fully spread         |
| `hover_float`    | HOVERING                  | Half-spread, oscillate |
| `wing_expand`    | Transition -> ELYTRA      | Fold->spread (0.5s)  |
| `wing_fold`      | Transition away from ELYTRA | Spread->fold (0.5s) |

Blockbench bone rig: `root` → `body`, `wing_left`, `wing_right`. Keyframe channels on `rotation.y/z` of wing bones.

---

## Resource Files

```
assets/jetlytra/
  lang/en_us.json
  models/item/jetpack.json            # flat 2D inventory sprite model
  models/geo/jetpack.geo.json         # GeckoLib geometry
  textures/item/jetpack.png           # inventory icon
  textures/geo/jetpack.png            # 3D model texture
  animations/jetpack.animation.json   # GeckoLib keyframes
  sounds.json
  sounds/jetpack_thrust.ogg
  sounds/jetpack_idle.ogg
  sounds/jetpack_toggle.ogg

data/jetlytra/
  recipes/jetpack.json

META-INF/
  neoforge.mods.toml
  accesstransformer.cfg               # expose LivingEntity.fallFlyingTicks
  services/
    dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint
      -> pl.siarko.jetlytra.controlify.JetlytraControlifyEntrypoint
jetlytra.mixins.json
```

---

## NeoForge Events Used

| Feature           | Event / Hook                         | Side   |
|-------------------|--------------------------------------|--------|
| Item registration | `DeferredRegister<Item>` mod bus     | Both   |
| Capability attach | `AttachCapabilitiesEvent<Entity>`    | Both   |
| Capability copy   | `PlayerEvent.Clone`                  | Server |
| Physics tick      | `PlayerTickEvent.Post`               | Server |
| Network packets   | `RegisterPayloadsEvent`              | Both   |
| Client input      | `ClientTickEvent.Pre`                | Client |
| Particle emission | `ClientTickEvent.Pre`                | Client |
| Key registration  | `RegisterKeyMappingsEvent`           | Client |
| HUD overlay       | `RegisterGuiLayersEvent`             | Client |
| Armor render      | `FMLClientSetupEvent` (GeckoLib reg) | Client |

---

## Implementation Phases

1. **Foundation** — build files, `@Mod` entry points, item in creative tab, flat sprite placeholder. Goal: item equips in chest slot.
2. **State & Network** — `FlightState`, capability, all 5 packet types, toggle with server→client sync.
3. **Physics** — thrust, hover, elytra mixin, `FuelSystem`, fuel HUD bar.
4. **Client Polish** — particles, sounds, mode label on HUD.
5. **3D Model** — Blockbench geometry + animations, GeckoLib renderers wired up.
6. **Controlify** — gamepad binds registered in entrypoint, OR-logic in input handler.

---

## Verification

After each phase: `./gradlew build` (compile) then `./gradlew client` (in-game).

1. Phase 1: item in creative tab, equips in chest slot
2. Phase 2: console log confirms state transitions on toggle
3. Phase 3: all four flight states work; fuel depletes; hover holds altitude
4. Phase 4: particles visible from behind during thrust
5. Phase 5: 3D model on player back; wings animate on sprint/crouch
6. Phase 6: Xbox/PS controller triggers same behavior as keyboard
