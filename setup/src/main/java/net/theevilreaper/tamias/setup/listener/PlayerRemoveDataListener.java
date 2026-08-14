package net.theevilreaper.tamias.setup.listener;

import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.dialog.MapDialogs;
import net.theevilreaper.tamias.setup.dialog.event.DialogContext;
import net.theevilreaper.tamias.setup.event.PlayerRemoveDataEvent;

import java.util.function.Consumer;

public class PlayerRemoveDataListener implements Consumer<PlayerRemoveDataEvent> {

    private final SetupDataService dataService;

    public PlayerRemoveDataListener(SetupDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void accept(PlayerRemoveDataEvent event) {
        this.dataService.get(event.getPlayer().getUuid()).ifPresent(data -> {
            DialogContext context = event.getContext();
            if (context == null) {
                MapDialogs.openDeleteDialog(event.getPlayer(), event.getMapDataCategory());
                return;
            }
            MapDialogs.openDeleteDialog(event.getPlayer(), event.getMapDataCategory(), context);
        });
    }
}
