package net.theevilreaper.tamias.setup.dialog.handler;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
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

        DoubleBinaryTag xTag = (DoubleBinaryTag) payload.get("x");
        DoubleBinaryTag yTag = (DoubleBinaryTag) payload.get("y");
        DoubleBinaryTag zTag = (DoubleBinaryTag) payload.get("z");
        if (xTag == null || yTag == null || zTag == null) return;

        Point point = new Pos(xTag.value(), yTag.value(), zTag.value());

        dataService.get(event.getPlayer().getUuid()).ifPresent(data -> {
            if (data instanceof InstanceSetupData instanceSetupData) {
                instanceSetupData.handleDataContextDelete(category, point);
            }
        });
    }
}
