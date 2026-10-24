package net.theevilreaper.tamias.setup;

import net.hollowcube.minestom.extensions.ExtensionBootstrap;
import net.theevilreaper.tamias.common.bootstrap.ServiceBootstrap;
import net.theevilreaper.tamias.common.permission.LuckPermsSupport;

public final class SetupServer {

    static void main() {
        // minestom-extensions loads platform extensions - the CloudNet bridge and our
        // :bridge permission extension among them - from the extensions/ folder. Running
        // standalone simply loads none. This also performs MinecraftServer.init(Auth), which is the
        // only point at which Velocity forwarding can still be turned on.
        ExtensionBootstrap bootstrap = ExtensionBootstrap.init(ServiceBootstrap.resolveAuth());
        LuckPermsSupport.bootstrap();
        TamiasSetup tamiasSetup = new TamiasSetup();

        tamiasSetup.initialize();
        ServiceBootstrap.installShutdownHandling();
        bootstrap.start(ServiceBootstrap.resolveBindHost(), ServiceBootstrap.resolveBindPort());
    }
}
