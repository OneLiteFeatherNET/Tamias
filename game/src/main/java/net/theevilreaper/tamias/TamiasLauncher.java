package net.theevilreaper.tamias;

import net.minestom.server.MinecraftServer;
import net.theevilreaper.tamias.game.Tamias;

public class TamiasLauncher {

    static void main() {
        MinecraftServer minecraftServer = MinecraftServer.init();
        Tamias tamias = new Tamias();
        tamias.initialize();
        minecraftServer.start("localhost", 25565);
    }
}
