package net.theevilreaper.tamias;

import net.hollowcube.minestom.extensions.ExtensionBootstrap;
import net.minestom.server.MinecraftServer;
import net.theevilreaper.tamias.common.bootstrap.ServiceBootstrap;
import net.theevilreaper.tamias.common.permission.LuckPermsSupport;
import net.theevilreaper.tamias.game.Tamias;

public class TamiasLauncher {

    static void main() {
        // minestom-extensions loads platform extensions - the CloudNet bridge and our
        // :bridge permission extension among them - from the extensions/ folder. Running
        // standalone simply loads none. This also performs MinecraftServer.init().
        ExtensionBootstrap bootstrap = ExtensionBootstrap.init();
        LuckPermsSupport.bootstrap();
        Tamias tamias = new Tamias();
        tamias.initialize();
        ServiceBootstrap.installShutdownHandling();
        bootstrap.start(ServiceBootstrap.resolveBindHost(), ServiceBootstrap.resolveBindPort());
    }
}
