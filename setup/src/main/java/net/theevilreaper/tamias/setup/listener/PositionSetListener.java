
package net.theevilreaper.tamias.setup.listener;

import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.event.PositionSetEvent;

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
                instanceSetupData.setPosition(event.getCategory(), event.getPlayer());
            }
        });
    }
}
