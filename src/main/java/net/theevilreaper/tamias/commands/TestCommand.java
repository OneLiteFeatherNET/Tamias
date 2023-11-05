package net.theevilreaper.tamias.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.theevilreaper.tamias.listener.game.PlayerInteractItemListener;
import net.theevilreaper.tamias.stamina.ExplodeBar;
import net.theevilreaper.tamias.stamina.ShootBar;
import net.theevilreaper.tamias.stamina.StaminaBar;
import net.theevilreaper.tamias.util.Items;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public class TestCommand extends Command {

    private final ArgumentWord argumentWord;

    private final Items items;

    private StaminaBar staminaBar;

    public TestCommand() {
        super("test", "t");
        this.argumentWord = ArgumentType.Word("test").from("A", "B");
        this.items = new Items();
        addSyntax(this::onCommand, argumentWord);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, new PlayerInteractItemListener(unused -> this.staminaBar));
    }

    private void onCommand(@NotNull CommandSender sender, @NotNull CommandContext context) {
        var player = (Player) sender;
        var argument = context.get(argumentWord);

        if ("A".equals(argument)) {
            this.items.setBombItem(player);
            player.sendMessage("Test A");
            this.staminaBar = new ExplodeBar(player);
        } else {
            this.items.setShootItem(player);
            player.sendMessage("Test B");
            this.staminaBar = new ShootBar(player);
        }
    }
}
