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
import net.theevilreaper.tamias.setup.util.SetupTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AuthorInputDialog implements DialogTemplate<List<String>> {

    public static final Key DIALOG_KEY = Key.key("suicide", "bounce_author_setup");

    private final Component header;
    private final Component submitComponent;
    private final Component cancelComponent;

    public AuthorInputDialog(Component header, Component submitComponent, Component cancelComponent) {
        this.header = header;
        this.submitComponent = submitComponent;
        this.cancelComponent = cancelComponent;
    }

    @Override
    public void open(Player player, @Nullable List<String> data) {
        int amount = player.getTag(SetupTags.AUTHOR_AMOUNT_TAG);
        List<DialogInput> inputFields = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            inputFields.add(
                    new DialogInput.Text(
                            "author" + i,
                            200,
                            Component.text("Author " + i),
                            false,
                            "",
                            32,
                            null
                    )
            );
        }
        var packet = new ShowDialogPacket(new Dialog.Confirmation(
                new DialogMetadata(
                        header,
                        null,
                        false,
                        false,
                        DialogAfterAction.CLOSE,
                        List.of(
                                new DialogBody.PlainMessage(Component.text("Please enter the builder(s)"), 200)
                        ),
                        inputFields
                ),
                new DialogActionButton(
                        submitComponent,
                        Component.text("Click to confirm", NamedTextColor.GREEN),
                        100,
                        new DialogAction.DynamicCustom(DIALOG_KEY,
                                CompoundBinaryTag.builder()
                                        .putInt("amount", amount)
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
    public Key key() {
        return DIALOG_KEY;
    }
}
