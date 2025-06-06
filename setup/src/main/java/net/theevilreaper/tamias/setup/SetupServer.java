package net.theevilreaper.tamias.setup;

import net.minestom.server.MinecraftServer;

public final class SetupServer {

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        TamiasSetup tamiasSetup = new TamiasSetup();

        tamiasSetup.initialize();
        server.start("localhost", 25565);
    }
}
