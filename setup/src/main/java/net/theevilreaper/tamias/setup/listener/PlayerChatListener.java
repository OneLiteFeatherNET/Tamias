package net.theevilreaper.tamias.setup.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.onelitefeather.guira.SetupDataService;
import net.onelitefeather.guira.data.SetupData;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.util.SetupTags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerChatListener implements Consumer<PlayerChatEvent> {

    private static final Component CHAT_SEPARATOR = Component.text("»", NamedTextColor.YELLOW);
    private static final Component SQUARE_OPEN = Component.text("[", NamedTextColor.GRAY);
    private static final Component SQUARE_CLOSE = Component.text("]", NamedTextColor.GRAY);

    private final SetupDataService setupDataService;

    public PlayerChatListener(@NotNull SetupDataService setupDataService) {
        this.setupDataService = setupDataService;
    }

    @Override
    public void accept(@NotNull PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!player.hasTag(SetupTags.SETUP_TAG)) {
            event.setFormattedMessage(formatGeneral(event));
        } else {
            event.setFormattedMessage(formatSetup(event));
        }
    }

    private @NotNull Component formatGeneral(@NotNull PlayerChatEvent event) {
        Player player = event.getPlayer();
        return Component.text(player.getUsername(), NamedTextColor.GREEN).append(Component.space())
                .append(CHAT_SEPARATOR).append(Component.space())
                .append(Component.text(event.getRawMessage(), NamedTextColor.GRAY));
    }

    private @NotNull Component formatSetup(@NotNull PlayerChatEvent event) {
        Player player = event.getPlayer();
        Component general = formatGeneral(event);
        SetupData setupData = setupDataService.get(player.getUuid()).orElse(null);
        if (!(setupData instanceof InstanceSetupData instanceData)) {
            return Component.empty();
        }
        Component mapName;

        BaseMap map = null;

        if (instanceData.hasMapFile() && map != null) {
            String mapAsString = map.getName();
            mapName = SQUARE_OPEN
                    .append(Component.text(mapAsString, NamedTextColor.LIGHT_PURPLE))
                    .append(SQUARE_CLOSE).append(Component.space());
        } else {
            mapName = SQUARE_OPEN
                    .append(Component.text("*", NamedTextColor.RED))
                    .append(SQUARE_CLOSE).append(Component.space());
        }
        return mapName.append(general);
    }
}
