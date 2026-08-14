package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.entity.Player;
import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.event.DirectionSetEvent;
import net.theevilreaper.tamias.setup.util.SetupMessages;
import net.theevilreaper.tamias.setup.util.SetupSounds;

import java.util.function.Consumer;

public class DirectionSetListener implements Consumer<DirectionSetEvent> {

    private final SetupDataService dataService;

    public DirectionSetListener(SetupDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void accept(DirectionSetEvent event) {
        this.dataService.get(event.getPlayer().getUuid()).ifPresent(data -> {
            if (data instanceof InstanceSetupData instanceSetupData) {
                instanceSetupData.setDirection(event.getCategory(), event.getPlayer(), event.getDirection());
                Player player = event.getPlayer();
                player.playSound(SetupSounds.DATA_SET);
                player.sendMessage(SetupMessages.getDirectionSet(event.getCategory(), event.getDirection()));
            }
        });
    }
}
