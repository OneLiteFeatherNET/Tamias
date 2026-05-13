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
import net.minestom.server.dialog.DialogInput;
import net.minestom.server.dialog.DialogMetadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.common.ShowDialogPacket;
import net.theevilreaper.tamias.setup.dialog.DialogTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AuthorRequestDialog implements DialogTemplate<Void> {

    public static final Key DIALOG_KEY = Key.key("suicide", "author_amount_dialog");

    private final Component header;
    private final Component submitComponent;
    private final Component cancelComponent;

    public AuthorRequestDialog(Component header, Component submitComponent, Component cancelComponent) {
        this.header = header;
        this.submitComponent = submitComponent;
        this.cancelComponent = cancelComponent;
    }

    @Override
    public void open(Player player, @Nullable Void data) {
        var packet = new ShowDialogPacket(new Dialog.Confirmation(
                new DialogMetadata(
                        header,
                        null,
                        false,
                        false,
                        DialogAfterAction.CLOSE,
                        List.of(
                                new DialogBody.PlainMessage(Component.text("How many builders should the map have?"), 200)
                        ),
                        List.of(
                                new DialogInput.NumberRange("amount", 200, Component.text("Amount"), "options.generic_value", 1, 10, 1f, 1f)
                        )
                ),
                new DialogActionButton(
                        submitComponent,
                        Component.text("Click to confirm", NamedTextColor.GREEN),
                        100,
                        new DialogAction.DynamicCustom(DIALOG_KEY, CompoundBinaryTag.builder().build())
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
    public Key key() {
        return DIALOG_KEY;
    }
}
