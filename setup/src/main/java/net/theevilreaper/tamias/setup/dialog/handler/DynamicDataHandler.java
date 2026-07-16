package net.theevilreaper.tamias.setup.dialog.handler;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

public final class DynamicDataHandler implements DialogHandler {

    private final SetupDataService dataService;

    public DynamicDataHandler(SetupDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(PlayerCustomClickEvent event, CompoundBinaryTag payload) {
        IntBinaryTag idTag = (IntBinaryTag) payload.get("category_id");
        if (idTag == null) return;
        int categoryId = idTag.value();
        MapDataCategory category = MapDataCategory.byId(categoryId);

        dataService.get(event.getPlayer().getUuid()).ifPresent(data -> {
        });

    }
}
