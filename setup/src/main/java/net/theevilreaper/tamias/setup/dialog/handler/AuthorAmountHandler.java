package net.theevilreaper.tamias.setup.dialog.handler;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.theevilreaper.tamias.setup.dialog.event.DialogContext;
import net.theevilreaper.tamias.setup.dialog.event.DialogRequestEvent;
import net.theevilreaper.tamias.setup.dialog.event.DialogTarget;

public final class AuthorAmountHandler implements DialogHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(PlayerCustomClickEvent event, CompoundBinaryTag payload) {
        FloatBinaryTag amountBinary = (FloatBinaryTag) payload.get("amount");
        if (amountBinary == null) return;
        float amount = amountBinary.value();

        if (amount == 0) return;
        EventDispatcher.call(new DialogRequestEvent(event.getPlayer(), DialogTarget.AUTHOR_INPUT, new DialogContext.AuthorAmount(amount)));
    }
}
