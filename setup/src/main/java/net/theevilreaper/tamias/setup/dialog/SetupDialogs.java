package net.theevilreaper.tamias.setup.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.dialog.DialogAction;
import net.minestom.server.dialog.DialogAfterAction;
import net.minestom.server.entity.Player;
import net.onelitefeather.pica.dialog.DialogTemplate;
import net.onelitefeather.pica.dialog.type.DialogType;
import net.theevilreaper.tamias.setup.inventory.DataType;
import net.theevilreaper.tamias.setup.util.SetupTags;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for handling custom dialogs which are related to the setup process.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
public final class SetupDialogs extends DialogBase {

    public static final Key NAME_INPUT_KEY = create("name_setup_dialog");
    public static final Key AUTHOR_INPUT_KEY = create("bounce_author_setup");
    public static final Key AUTHOR_REQUEST_KEY = create("author_amount_dialog");
    public static final Key DELETE_KEY = create("delete_dialog");

    /**
     * Opens a dialog to enter the name of a map.
     *
     * @param player      the player who should see the dialog
     * @param initialName the initial value to pre-fill the input field, or {@code null} for an empty field
     */
    public static void openNameInput(Player player, @Nullable String initialName) {
        String initial = initialName == null ? "" : initialName;
        DialogTemplate dialogTemplate = DialogType.confirm(NAME_INPUT_KEY)
                .meta(meta -> {
                    meta.closeWithEscape(false);
                    meta.pause(false);
                    meta.afterAction(DialogAfterAction.CLOSE);
                    meta.title(Component.text("Map Name"));
                    meta.emptyMessage();
                    meta.messageBody(body -> body.width(200).contents(Component.text("How should the map be named?")));
                    meta.text("name", text -> text.maxLength(32).initial(initial));
                })
                .yesButton(button -> button.width(100).label(Component.text("Submit"))
                        .action(new DialogAction.DynamicCustom(NAME_INPUT_KEY, getEmptyPayload()))
                )
                .noButton(button -> button.width(101).label(NO_COMPONENT))
                .build();
        dialogTemplate.open(player);
    }

    /**
     * Opens a dialog to enter the authors of a map.
     * The number of input fields is determined by {@link SetupTags#AUTHOR_AMOUNT_TAG} on the player.
     *
     * @param player the player who should see the dialog
     */
    public static void openAuthorInput(Player player) {
        int amount = player.getTag(SetupTags.AUTHOR_AMOUNT_TAG);
        DialogTemplate dialogTemplate = DialogType.confirm(AUTHOR_INPUT_KEY)
                .meta(meta -> {
                    meta.closeWithEscape(false);
                    meta.pause(false);
                    meta.afterAction(DialogAfterAction.CLOSE);
                    meta.title(Component.text("Map Authors"));
                    meta.emptyMessage();
                    meta.messageBody(body -> body.width(200).contents(Component.text("Please enter the builder(s)")));
                    for (int i = 0; i < amount; i++) {
                        final int index = i;
                        meta.text("author" + index, text -> text.maxLength(32).initial(""));
                    }
                })
                .yesButton(button -> button.width(100).label(Component.text("Confirm"))
                        .action(new DialogAction.DynamicCustom(AUTHOR_INPUT_KEY,
                                CompoundBinaryTag.builder().putInt("amount", amount).build()
                        ))
                )
                .noButton(button -> button.width(101).label(NO_COMPONENT))
                .build();
        dialogTemplate.open(player);
    }

    /**
     * Opens a dialog to request the number of authors a map should have.
     *
     * @param player the player who should see the dialog
     */
    public static void openAuthorRequest(Player player) {
        DialogTemplate dialogTemplate = DialogType.confirm(AUTHOR_REQUEST_KEY)
                .meta(meta -> {
                    meta.closeWithEscape(false);
                    meta.pause(false);
                    meta.afterAction(DialogAfterAction.CLOSE);
                    meta.title(Component.text("Author Count"));
                    meta.emptyMessage();
                    meta.messageBody(body -> body.width(200).contents(Component.text("How many builders should the map have?")));
                    meta.range("amount", range -> range.initial(1).start(1f).end(10f).step(1f));
                })
                .yesButton(button -> button.width(100).label(Component.text("Confirm"))
                        .action(new DialogAction.DynamicCustom(AUTHOR_REQUEST_KEY, getEmptyPayload()))
                )
                .noButton(button -> button.width(101).label(NO_COMPONENT))
                .build();
        dialogTemplate.open(player);
    }

    /**
     * Opens a dialog to confirm the deletion of the given data type.
     *
     * @param player   the player who should see the dialog
     * @param dataType the data type to be deleted
     */
    public static void openDeleteConfirm(Player player, DataType dataType) {
        DialogTemplate dialogTemplate = DialogType.confirm(DELETE_KEY)
                .meta(meta -> {
                    meta.closeWithEscape(false);
                    meta.pause(false);
                    meta.afterAction(DialogAfterAction.CLOSE);
                    meta.title(Component.text("Confirm data deletion"));
                    meta.emptyMessage();
                    meta.messageBody(body -> body.width(150).contents(Component.text("The following map data would be deleted:")));
                    meta.emptyMessage();
                    meta.messageBody(body -> body.width(400).contents(Component.text("This action cannot be undone!", NamedTextColor.RED)));
                })
                .yesButton(button -> button.width(100).label(Component.text("Yes"))
                        .action(new DialogAction.DynamicCustom(DELETE_KEY,
                                CompoundBinaryTag.builder().putInt("type", dataType.ordinal()).build()
                        ))
                )
                .noButton(button -> button.width(101).label(NO_COMPONENT))
                .build();
        dialogTemplate.open(player);
    }

    private SetupDialogs() {
        /* Utility class */
    }
}