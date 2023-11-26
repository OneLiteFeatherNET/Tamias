package net.theevilreaper.tamias.setup;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SetupValidations {

    public static boolean argCondition(boolean condition, @NotNull CommandSender sender, @NotNull Component reason) {
        if (condition) {
            sender.sendMessage(reason);
            return true;
        }
        return false;
    }
}
