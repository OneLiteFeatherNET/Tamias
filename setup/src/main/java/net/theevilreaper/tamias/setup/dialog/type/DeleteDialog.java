package net.theevilreaper.tamias.setup.dialog.type;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.dialog.DialogAction;
import net.minestom.server.dialog.DialogActionButton;
import net.minestom.server.dialog.DialogAfterAction;
import net.minestom.server.dialog.DialogBody;
import net.minestom.server.dialog.DialogMetadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.common.ShowDialogPacket;
import net.theevilreaper.tamias.setup.dialog.AbstractDialogTemplate;
import net.theevilreaper.tamias.setup.inventory.DataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeleteDialog extends AbstractDialogTemplate<DataType> {

    public static final Key DIALOG_KEY = Key.key("bounce", "delete_dialog");

    public DeleteDialog() {
        super(Component.text("Confirm data deletion"), Component.text("Yes"), Component.text("No"));
    }

    @Override
    public void open(@NotNull Player player, @Nullable DataType data) {
        var packet = new ShowDialogPacket(new Dialog.Confirmation(
                new DialogMetadata(
                        header,
                        null,
                        false,
                        false,
                        DialogAfterAction.CLOSE,
                        List.of(
                                new DialogBody.PlainMessage(Component.text("The following map data would be deleted:"), 150),
                                new DialogBody.PlainMessage(Component.empty(), 1)
                               // new DialogBody.PlainMessage(Component.text(data == null ? "No data provided" : data.getName()), 100)
                        ),
                        List.of()
                ),
                new DialogActionButton(
                        submitComponent,
                        Component.text("Click to confirm", NamedTextColor.GREEN),
                        100,
                        new DialogAction.DynamicCustom(DIALOG_KEY, CompoundBinaryTag
                                .builder()
                                .putInt("type", data.ordinal())
                                .build()
                        )
                ),
                new DialogActionButton(
                        cancelComponent,
                        Component.text("Click to cancel", NamedTextColor.RED),
                        101,
                        null
                )
        ));
        player.sendPacket(packet);
    }

    @Override
    public @NotNull Key key() {
        return DIALOG_KEY;
    }
}
