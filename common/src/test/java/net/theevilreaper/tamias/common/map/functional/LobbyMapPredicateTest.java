package net.theevilreaper.tamias.common.map.functional;

import net.theevilreaper.aves.map.MapEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class LobbyMapPredicateTest {

    @Test
    void testLobbyMapPredicate() {
        LobbyMapPredicate predicate = new LobbyMapPredicate();

        Path path = Paths.get("");
        MapEntry falseCase = MapEntry.of(path);
        assertFalse(predicate.test(falseCase));
    }
}
