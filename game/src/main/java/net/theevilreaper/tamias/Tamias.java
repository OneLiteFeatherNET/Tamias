package net.theevilreaper.tamias;

import net.minestom.server.MinecraftServer;
import net.theevilreaper.tamias.game.TamiasGame;
import net.theevilreaper.tamias.game.util.FileChecker;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Tamias {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = new MinecraftServer();
        Path rootPath = Paths.get("");
        FileChecker.checkFileIntegrity(rootPath);
        new TamiasGame(rootPath);
        minecraftServer.start("localhost", 25565);

    }
}
