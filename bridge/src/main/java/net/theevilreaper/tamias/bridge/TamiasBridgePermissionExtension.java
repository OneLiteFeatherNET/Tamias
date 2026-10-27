package net.theevilreaper.tamias.bridge;

import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.impl.platform.minestom.MinestomPermissionChecker;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.util.TriState;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.extensions.Extension;
import net.onelitefeather.minestom.extensions.processor.ExtensionInfo;
import net.theevilreaper.tamias.common.event.GameStartEvent;

/**
 * Minestom extension that teaches the CloudNet bridge how Tamias resolves permissions.
 * <p>
 * The bridge ships a default checker that only inspects {@code player.getPermissionLevel()}, which
 * is always {@code 0} on a LuckPerms-managed server - maintenance bypass and task-level
 * {@code requiredPermission} checks would therefore reject every player, staff included. This
 * extension registers a checker that reads Adventure's {@link PermissionChecker#POINTER} instead,
 * the same pointer LuckPerms answers through, and marks it the registry default.
 * <p>
 * {@link MinestomPermissionChecker} only exists inside the CloudNet bridge's extension classloader,
 * so this glue cannot live in the application. The {@code CloudNet_Bridge} dependency declared below
 * makes this extension load after the bridge and share its classloader hierarchy.
 * <p>
 * The same extension also reports the game as in game: on {@link GameStartEvent}, which is called once
 * the lobby phase finishes, it switches the service to {@code INGAME} so CloudNet starts a fresh service
 * of the task. A jar carries only one extension entry point, so this lives here and shares the
 * {@code CloudNet_Bridge} dependency.
 * <p>
 * {@link ExtensionInfo} generates {@code extension.json} at compile time; the version is supplied by
 * the build through {@code -Aminestom.extension.version}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtensionInfo(
        name = "TamiasCloudNetPermissions",
        authors = "OneLiteFeather",
        dependencies = "CloudNet_Bridge"
)
public final class TamiasBridgePermissionExtension extends Extension {

    private final EventNode<Event> inGameNode = EventNode.all("tamias-bridge-ingame");

    @Override
    public void initialize() {
        MinestomPermissionChecker checker = (player, permission) ->
                player.getOrDefault(PermissionChecker.POINTER, PermissionChecker.always(TriState.FALSE))
                        .test(permission);
        ServiceRegistry.registry()
                .registerProvider(MinestomPermissionChecker.class, "tamias-luckperms", checker)
                .markAsDefaultService();

        inGameNode.addListener(GameStartEvent.class, new InGameListener(new CloudNetServiceStateSwitch()));
        MinecraftServer.getGlobalEventHandler().addChild(inGameNode);
    }

    @Override
    public void terminate() {
        MinecraftServer.getGlobalEventHandler().removeChild(inGameNode);
    }
}
