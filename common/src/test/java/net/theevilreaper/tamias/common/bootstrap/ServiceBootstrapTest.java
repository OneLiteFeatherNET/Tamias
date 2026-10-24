package net.theevilreaper.tamias.common.bootstrap;

import net.minestom.server.Auth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ServiceBootstrapTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("service.bind.host");
        System.clearProperty("service.bind.port");
    }

    @Test
    @DisabledIfSystemProperty(named = "service.bind.host", matches = ".+")
    void testDefaultBindHost() {
        assertEquals("localhost", ServiceBootstrap.resolveBindHost());
    }

    @Test
    @DisabledIfSystemProperty(named = "service.bind.port", matches = ".+")
    void testDefaultBindPort() {
        assertEquals(25565, ServiceBootstrap.resolveBindPort());
    }

    @Test
    void testBindHostFromSystemProperty() {
        System.setProperty("service.bind.host", "0.0.0.0");
        assertEquals("0.0.0.0", ServiceBootstrap.resolveBindHost());
    }

    @Test
    void testBindPortFromSystemProperty() {
        System.setProperty("service.bind.port", "30000");
        assertEquals(30000, ServiceBootstrap.resolveBindPort());
    }

    @AfterEach
    void clearWorkingDirProperty() {
        System.clearProperty("service.working.dir");
    }

    @Test
    @DisabledIfSystemProperty(named = "service.working.dir", matches = ".+")
    void testDefaultWorkingDirectory() {
        assertEquals(Paths.get(""), ServiceBootstrap.resolveWorkingDirectory());
    }

    @Test
    void testWorkingDirectoryFromSystemProperty() {
        System.setProperty("service.working.dir", "/app");
        assertEquals(Paths.get("/app"), ServiceBootstrap.resolveWorkingDirectory());
    }

    @Test
    void testAuthDefaultsToOfflineWithoutVelocitySecret() {
        assertInstanceOf(Auth.Offline.class, ServiceBootstrap.resolveAuth(null));
    }

    @Test
    void testAuthUsesVelocityWhenSecretIsSet() {
        assertEquals(new Auth.Velocity("a-velocity-secret"), ServiceBootstrap.resolveAuth("a-velocity-secret"));
    }

    @Test
    void testAuthTrimsVelocitySecret() {
        assertEquals(new Auth.Velocity("a-velocity-secret"), ServiceBootstrap.resolveAuth("  a-velocity-secret\n"));
    }

    @Test
    void testAuthFallsBackToOfflineOnBlankVelocitySecret() {
        assertInstanceOf(Auth.Offline.class, ServiceBootstrap.resolveAuth("   "));
    }
}
