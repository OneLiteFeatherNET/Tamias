package net.theevilreaper.tamias.setup.listener.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.onelitefeather.guira.functional.OptionalSetupDataGetter;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.dialog.DialogRegistry;
import net.theevilreaper.tamias.setup.dialog.DialogTemplate;
import net.theevilreaper.tamias.setup.dialog.type.AuthorInputDialog;
import net.theevilreaper.tamias.setup.dialog.type.AuthorRequestDialog;
import net.theevilreaper.tamias.setup.dialog.type.DeleteDialog;
import net.theevilreaper.tamias.setup.dialog.type.NameInputDialog;
import net.theevilreaper.tamias.setup.inventory.DataType;
import net.theevilreaper.tamias.setup.util.SetupTags;

import java.util.function.Consumer;

public class PlayerCustomClickEventListener implements Consumer<PlayerCustomClickEvent> {

    private final DialogRegistry dialogRegistry;
    private final OptionalSetupDataGetter setupDataGetter;

    public PlayerCustomClickEventListener(DialogRegistry dialogRegistry, OptionalSetupDataGetter setupDataGetter) {
        this.dialogRegistry = dialogRegistry;
        this.setupDataGetter = setupDataGetter;
    }

    @Override
    public void accept(PlayerCustomClickEvent event) {
        Player player = event.getPlayer();

        if (!player.hasTag(SetupTags.SETUP_TAG)) return;

        Key key = event.getKey();
        BinaryTag payload = event.getPayload();

        if (payload == null) return;

        DialogTemplate<?> dialogTemplate = dialogRegistry.get(key);

        if (dialogTemplate == null) return;

        CompoundBinaryTag dialogData = (CompoundBinaryTag) payload;

        if (dialogTemplate instanceof AuthorRequestDialog) {
            int amount = dialogData.getInt("amount", 1);
            player.setTag(SetupTags.AUTHOR_AMOUNT_TAG, amount);
            dialogRegistry.get(AuthorInputDialog.DIALOG_KEY).open(player);
            return;
        }

        setupDataGetter.get(player.getUuid()).ifPresent(setupData -> {
            InstanceSetupData data = (InstanceSetupData) setupData;

            switch (dialogTemplate) {
                case NameInputDialog _ -> this.handleNameSet(data, dialogData);
                case AuthorInputDialog _ -> handleAuthorSet(data, dialogData);
                case DeleteDialog _ -> this.handleDataDelete(data, dialogData);
                default ->
                        throw new IllegalStateException("Unexpected dialog type: " + dialogTemplate.getClass().getCanonicalName());
            }
        });
    }

    /**
     * Handles the setting of authors based on the dialog data provided.
     *
     * @param data       the BounceData instance containing the map builder
     * @param dialogData the dialog data containing the authors to set
     */
    private void handleNameSet(InstanceSetupData data, CompoundBinaryTag dialogData) {
        String name = dialogData.getString("name");
        if (name.trim().isEmpty()) return;
        data.getMapBuilder().name(name);
        data.triggerUpdate();
    }

    /**
     * Handles the setting of authors based on the dialog data provided.
     *
     * @param data       the BounceData instance containing the map builder
     * @param dialogData the dialog data containing the authors to set
     */
    private void handleAuthorSet(InstanceSetupData data, CompoundBinaryTag dialogData) {
        int amount = dialogData.getInt("amount", 1);

        for (int i = 0; i < amount; i++) {
            String author = dialogData.getString("author" + i);
            if (author.trim().isEmpty()) continue;
            data.getMapBuilder().builder(author);
        }
        data.triggerUpdate();
    }

    /**
     * Handles the deletion of data based on the dialog data provided.
     *
     * @param data       the BounceData instance containing the map builder
     * @param dialogData the dialog data containing the type of data to delete
     */
    private void handleDataDelete(InstanceSetupData data, CompoundBinaryTag dialogData) {
        int type = dialogData.getInt("type");
        DataType mappedType = DataType.fromOrdinal(type);
        switch (mappedType) {
            case NAME -> data.getMapBuilder().name(null);
            case SPAWN -> data.getMapBuilder().spawn(null);
        }

        data.triggerUpdate();
    }
}
