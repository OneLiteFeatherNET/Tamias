
package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.entity.Player;
import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.event.PositionSetEvent;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import net.theevilreaper.tamias.setup.util.SetupMessages;
import net.theevilreaper.tamias.setup.util.SetupSounds;

import java.util.function.Consumer;

public class PositionSetListener implements Consumer<PositionSetEvent> {

    private final SetupDataService dataService;

    public PositionSetListener(SetupDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void accept(PositionSetEvent event) {
        this.dataService.get(event.getPlayer().getUuid()).ifPresent(data -> {
            if (data instanceof InstanceSetupData instanceSetupData) {
                Player player = event.getPlayer();
                if (isAreaCorner(event.getCategory()) && !player.isOnGround()) {
                    player.sendMessage(SetupMessages.NOT_ON_GROUND);
                    return;
                }
                instanceSetupData.setPosition(event.getCategory(), player);
                player.playSound(SetupSounds.DATA_SET);
                player.sendMessage(SetupMessages.getPositionSet(event.getCategory()));
            }
        });
    }

    /**
     * Checks if the given category is one of the area corners, which must be set while standing on the ground.
     *
     * @param category the category to check
     * @return true if the category is an area corner
     */
    private static boolean isAreaCorner(MapDataCategory category) {
        return category == MapDataCategory.AREA_LOWER_CORNER || category == MapDataCategory.AREA_UPPER_CORNER;
    }
}
