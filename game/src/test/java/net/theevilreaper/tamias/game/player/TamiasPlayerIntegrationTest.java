package net.theevilreaper.tamias.game.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TamiasPlayerIntegrationTest extends TamiasPlayerTestBase {

    private static final Pos BOMBER_POS = new Pos(0, 40, 0);
    private static final Pos VIEWER_POS = new Pos(2, 40, 0);

    @Test
    void testPrimedFuseOutlastsExplodeCountdown() {
        // The client discards a primed TNT by itself once its fuse runs out. The ExplodeBar needs 11 consume calls
        // every 250 ms (plus up to one period until the first call) before the server hides the bomber, so the
        // fuse must outlast 11 * 250 ms or viewers see the bomber vanish before the explosion.
        long fuseMillis = TamiasPlayer.PRIMED_FUSE_TIME * 50L;
        long countdownMillis = 11 * 250L;
        assertTrue(fuseMillis > countdownMillis, "fuse " + fuseMillis + "ms must outlast countdown " + countdownMillis + "ms");
    }

    @Test
    void testRoundDataDefaults(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);

        assertEquals(RoundRole.NONE, player.getStartRole());
        assertEquals(0, player.getRoundWins());
        assertEquals(0, player.getKills());
        assertEquals(0, player.getHits());

        player.setStartRole(RoundRole.BOMBER);
        player.addRoundWin();
        player.addRoundWin();
        player.addKill();
        player.addHit();
        player.addHit();
        player.addHit();

        assertEquals(RoundRole.BOMBER, player.getStartRole());
        assertEquals(2, player.getRoundWins());
        assertEquals(1, player.getKills());
        assertEquals(3, player.getHits());

        env.destroyInstance(instance, true);
    }

    @Test
    void testDefaultStateIsNone(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, BOMBER_POS);

        TamiasPlayer tamiasPlayer = assertInstanceOf(TamiasPlayer.class, player);
        assertEquals(DisguiseState.NONE, tamiasPlayer.getDisguiseState());
        assertEquals(EntityType.PLAYER, tamiasPlayer.getEntityType());

        env.destroyInstance(instance, true);
    }

    @Test
    void testTntBlockSpawnsFallingBlockForViewer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");

        Collector<DestroyEntitiesPacket> destroys = viewerConnection.trackIncoming(DestroyEntitiesPacket.class);
        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);
        Collector<EntityMetaDataPacket> metas = viewerConnection.trackIncoming(EntityMetaDataPacket.class);

        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        assertEquals(DisguiseState.TNT_BLOCK, bomber.getDisguiseState());
        assertEquals(EntityType.PLAYER, bomber.getEntityType());
        destroys.assertAnyMatch(packet -> packet.entityIds().contains(bomber.getEntityId()));
        spawns.assertSingle(packet -> {
            assertEquals(bomber.getEntityId(), packet.entityId());
            assertEquals(EntityType.FALLING_BLOCK, packet.type());
            assertEquals(Block.TNT.stateId(), packet.data());
        });
        metas.assertAnyMatch(packet -> packet.entityId() == bomber.getEntityId()
                && packet.entries().containsKey(MetadataDef.HAS_NO_GRAVITY.index()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testPrimedTntSpawnsTntWithFuse(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);
        Collector<EntityMetaDataPacket> metas = viewerConnection.trackIncoming(EntityMetaDataPacket.class);

        bomber.setDisguise(DisguiseState.PRIMED_TNT);

        spawns.assertSingle(packet -> {
            assertEquals(bomber.getEntityId(), packet.entityId());
            assertEquals(EntityType.TNT, packet.type());
        });
        metas.assertAnyMatch(packet -> packet.entityId() == bomber.getEntityId()
                && packet.entries().containsKey(MetadataDef.PrimedTnt.FUSE_TIME.index())
                && Integer.valueOf(TamiasPlayer.PRIMED_FUSE_TIME)
                .equals(packet.entries().get(MetadataDef.PrimedTnt.FUSE_TIME.index()).value()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testHiddenSendsOnlyDestroy(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.PRIMED_TNT);

        Collector<DestroyEntitiesPacket> destroys = viewerConnection.trackIncoming(DestroyEntitiesPacket.class);
        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);

        bomber.setDisguise(DisguiseState.HIDDEN);

        destroys.assertAnyMatch(packet -> packet.entityIds().contains(bomber.getEntityId()));
        spawns.assertEmpty();

        env.destroyInstance(instance, true);
    }

    @Test
    void testNoneSpawnsPlayerAgain(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.HIDDEN);

        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);

        bomber.setDisguise(DisguiseState.NONE);

        spawns.assertSingle(packet -> {
            assertEquals(bomber.getEntityId(), packet.entityId());
            assertEquals(EntityType.PLAYER, packet.type());
        });

        env.destroyInstance(instance, true);
    }

    @Test
    void testSameStateSendsNothing(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        Collector<DestroyEntitiesPacket> destroys = viewerConnection.trackIncoming(DestroyEntitiesPacket.class);
        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);

        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        destroys.assertEmpty();
        spawns.assertEmpty();

        env.destroyInstance(instance, true);
    }

    @Test
    void testNewViewerSeesDisguise(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        TestConnection viewerConnection = env.createConnection();
        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);
        viewerConnection.connect(instance, VIEWER_POS);
        env.tick();

        spawns.assertAnyMatch(packet -> packet.entityId() == bomber.getEntityId()
                && packet.type() == EntityType.FALLING_BLOCK);
        spawns.assertNoneMatch(packet -> packet.entityId() == bomber.getEntityId()
                && packet.type() == EntityType.PLAYER);

        env.destroyInstance(instance, true);
    }

    @Test
    void testNewViewerSeesNothingWhileHidden(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        bomber.setDisguise(DisguiseState.HIDDEN);

        TestConnection viewerConnection = env.createConnection();
        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);
        viewerConnection.connect(instance, VIEWER_POS);
        env.tick();

        spawns.assertNoneMatch(packet -> packet.entityId() == bomber.getEntityId());

        env.destroyInstance(instance, true);
    }

    @Test
    void testBoundingBoxFollowsDisguise(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        double playerHeight = bomber.getBoundingBox().height();

        bomber.setDisguise(DisguiseState.TNT_BLOCK);
        assertEquals(0.98, bomber.getBoundingBox().width());
        assertEquals(0.98, bomber.getBoundingBox().height());
        assertEquals(0.98, bomber.getBoundingBox().depth());

        bomber.setDisguise(DisguiseState.HIDDEN);
        assertEquals(0.98, bomber.getBoundingBox().height());

        bomber.setDisguise(DisguiseState.NONE);
        assertEquals(playerHeight, bomber.getBoundingBox().height());

        env.destroyInstance(instance, true);
    }

    @Test
    void testEquipmentNotSentWhileDisguised(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        Collector<EntityEquipmentPacket> equipment = viewerConnection.trackIncoming(EntityEquipmentPacket.class);

        bomber.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.TNT));

        equipment.assertNoneMatch(packet -> packet.entityId() == bomber.getEntityId());

        env.destroyInstance(instance, true);
    }

    @Test
    void testEquipmentSentWithoutDisguise(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");

        Collector<EntityEquipmentPacket> equipment = viewerConnection.trackIncoming(EntityEquipmentPacket.class);

        bomber.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.TNT));

        equipment.assertAnyMatch(packet -> packet.entityId() == bomber.getEntityId());

        env.destroyInstance(instance, true);
    }

    @Test
    void testMetadataReplacedWhileDisguised(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        Collector<EntityMetaDataPacket> metas = viewerConnection.trackIncoming(EntityMetaDataPacket.class);

        bomber.setSneaking(true);

        metas.assertAnyMatch(packet -> packet.entityId() == bomber.getEntityId());
        metas.assertAllMatch(packet -> packet.entityId() != bomber.getEntityId()
                || packet.entries().keySet().equals(java.util.Set.of(MetadataDef.HAS_NO_GRAVITY.index())));

        env.destroyInstance(instance, true);
    }

    @Test
    void testHiddenSendsNoViewerPackets(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.HIDDEN);

        Collector<EntityMetaDataPacket> metas = viewerConnection.trackIncoming(EntityMetaDataPacket.class);
        Collector<EntityEquipmentPacket> equipment = viewerConnection.trackIncoming(EntityEquipmentPacket.class);

        bomber.setSneaking(true);
        bomber.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.TNT));

        metas.assertNoneMatch(packet -> packet.entityId() == bomber.getEntityId());
        equipment.assertNoneMatch(packet -> packet.entityId() == bomber.getEntityId());

        env.destroyInstance(instance, true);
    }

    @Test
    void testTeleportKeepsDisguise(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer bomber = (TamiasPlayer) env.createPlayer(instance, BOMBER_POS);
        TestConnection viewerConnection = env.createConnection();
        Player viewer = viewerConnection.connect(instance, VIEWER_POS);
        env.tick();
        assertTrue(bomber.getViewers().contains(viewer), "precondition: viewer must see the bomber");
        bomber.setDisguise(DisguiseState.TNT_BLOCK);

        Collector<SpawnEntityPacket> spawns = viewerConnection.trackIncoming(SpawnEntityPacket.class);
        Collector<EntityMetaDataPacket> metas = viewerConnection.trackIncoming(EntityMetaDataPacket.class);

        bomber.teleport(new Pos(4, 40, 4)).join();
        env.tick();

        spawns.assertNoneMatch(packet -> packet.entityId() == bomber.getEntityId()
                && packet.type() == EntityType.PLAYER);
        metas.assertAllMatch(packet -> packet.entityId() != bomber.getEntityId()
                || packet.entries().keySet().equals(java.util.Set.of(MetadataDef.HAS_NO_GRAVITY.index())));

        env.destroyInstance(instance, true);
    }
}
