# Tamias Bridge

Minestom extension that connects Tamias to CloudNet's bridge module. It does two things.

## Permissions

It registers a `MinestomPermissionChecker` backed by Adventure's `PermissionChecker.POINTER`,
the same pointer `PermissionAwarePlayer` installs and LuckPerms answers through.

CloudNet's bridge ships a default permission checker that only inspects
`player.getPermissionLevel()`, which is always `0` on a LuckPerms-managed server. Without this
extension, CloudNet's maintenance-mode bypass and any task-level `requiredPermission` check
reject every player, staff included.

## INGAME service state

`PlayingPhase.onStart()` dispatches a `RoundInGameEvent` (from `:common`) once the game logic of
the round has started. `InGameListener` reacts to it, once per service lifetime, by calling
`BridgeServiceHelper.changeToIngame(true)` followed by `ServiceInfoHolder.publishServiceInfoUpdate()`.
The service is then `INGAME` for the whole cluster and CloudNet starts a fresh service of the task,
so new players land on a lobby-state service instead of this running game.

Failures (for example no CloudNet around when running standalone) are logged and never reach the
game thread. A failed switch is not retried, so a failure cannot start a second service.

## Deployment

This module is never bundled into the game or setup fat jars. Build it and drop the resulting
jar into the CloudNet service's `extensions/` folder, next to the `CloudNet_Bridge` extension
it depends on (declared via `dependencies = "CloudNet_Bridge"` on
`TamiasBridgePermissionExtension`, so it will refuse to load before the bridge is present):

```
./gradlew :bridge:build
cp bridge/build/libs/bridge-*.jar <cloudnet-service>/extensions/
```

If the jar is missing, the server boots and runs normally, but CloudNet falls back to its
`getPermissionLevel()`-based checker and the service never switches to `INGAME`. No log line flags
a missing extension.
