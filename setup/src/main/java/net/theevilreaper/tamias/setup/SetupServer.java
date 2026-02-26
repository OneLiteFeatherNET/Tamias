package net.theevilreaper.tamias.setup;

import net.minestom.server.MinecraftServer;

public final class SetupServer {

    static void main() {
        MinecraftServer server = MinecraftServer.init();

        TamiasSetup tamiasSetup = new TamiasSetup();

        tamiasSetup.initialize();
        server.start("localhost", 25565);
    }
}
