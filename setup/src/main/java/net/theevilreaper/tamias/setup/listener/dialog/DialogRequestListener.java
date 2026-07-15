package net.theevilreaper.tamias.setup.listener.dialog;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.setup.dialog.AuthorDialogs;
import net.theevilreaper.tamias.setup.dialog.MapDialogs;
import net.theevilreaper.tamias.setup.dialog.event.DialogContext;
import net.theevilreaper.tamias.setup.dialog.event.DialogRequestEvent;
import net.theevilreaper.tamias.setup.dialog.event.DialogTarget;

import java.util.function.Consumer;

public class DialogRequestListener implements Consumer<DialogRequestEvent> {

    @Override
    public void accept(DialogRequestEvent event) {
        DialogTarget target = event.getTarget();
        Player player = event.getPlayer();
        DialogContext context = event.getContext();

        switch (target) {
            case CREATE_NAME -> MapDialogs.openNameCreateDialog(player);
            case UPDATE_NAME -> {
                if (context == null) return;
                MapDialogs.openNameUpdateDialog(player, ((DialogContext.NameContext) context).name());
            }
            case CREATE_AUTHORS -> AuthorDialogs.openAuthorRequestDialog(player);
            case AUTHOR_INPUT -> {
                if (context == null) return;
                AuthorDialogs.openAuthorInput(player, ((DialogContext.AuthorAmount)context).amount());
            }
            default -> {
                // Nothing to do here
            }
        }
    }
}
