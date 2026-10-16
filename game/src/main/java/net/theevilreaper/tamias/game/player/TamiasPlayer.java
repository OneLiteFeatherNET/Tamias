package net.theevilreaper.tamias.game.player;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityAttributesPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.theevilreaper.tamias.common.player.PermissionAwarePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link TamiasPlayer} can be disguised for its viewers without changing the server-side entity type.
 * <p>
 * While disguised, viewers receive a different entity under this player's own entity id. Minestom keeps sending
 * the player's movement packets under that id, so the disguise follows the player without any extra sync.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TamiasPlayer extends PermissionAwarePlayer {

    // Longer than the ExplodeBar countdown (2.5-2.75 s): the client discards a primed TNT on its own once the fuse
    // runs out, the server hides the bomber at the explosion anyway
    static final int PRIMED_FUSE_TIME = 60;
    static final BoundingBox DISGUISE_BOUNDING_BOX = new BoundingBox(0.98, 0.98, 0.98);

    private final BoundingBox playerBoundingBox;
    private DisguiseState disguiseState = DisguiseState.NONE;
    private RoundRole startRole = RoundRole.NONE;
    private int roundWins;
    private int kills;
    private int hits;

    /**
     * {@inheritDoc}
     */
    public TamiasPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        this.playerBoundingBox = getBoundingBox();
    }

    /**
     * Changes what viewers see in place of this player.
     * Current viewers get the old entity destroyed and the new disguise (or the player, or nothing) spawned.
     *
     * @param state the new disguise state
     */
    public void setDisguise(DisguiseState state) {
        if (this.disguiseState == state) return;

        List<Player> currentViewers = List.copyOf(getViewers());
        DestroyEntitiesPacket destroyPacket = new DestroyEntitiesPacket(List.of(getEntityId()));
        currentViewers.forEach(viewer -> viewer.sendPacket(destroyPacket));

        this.disguiseState = state;
        setBoundingBox(state == DisguiseState.NONE ? this.playerBoundingBox : DISGUISE_BOUNDING_BOX);

        currentViewers.forEach(this::updateNewViewer);
    }

    /**
     * Returns the current disguise state.
     *
     * @return the disguise state
     */
    public DisguiseState getDisguiseState() {
        return this.disguiseState;
    }

    /**
     * Sets the role this player had when the current round started.
     *
     * @param startRole the role at round start
     */
    public void setStartRole(RoundRole startRole) {
        this.startRole = startRole;
    }

    /**
     * Returns the role this player had when the current round started.
     *
     * @return the role at round start
     */
    public RoundRole getStartRole() {
        return this.startRole;
    }

    /**
     * Adds a won round to this player's stats.
     */
    public void addRoundWin() {
        this.roundWins++;
    }

    /**
     * Returns the amount of rounds this player has won.
     *
     * @return the won rounds
     */
    public int getRoundWins() {
        return this.roundWins;
    }

    /**
     * Adds a kill (a survivor converted by this player's explosion) to this player's stats.
     */
    public void addKill() {
        this.kills++;
    }

    /**
     * Returns the amount of survivors this player has converted as a bomber.
     *
     * @return the kills
     */
    public int getKills() {
        return this.kills;
    }

    /**
     * Adds a hit (an egg which made a bomber explode) to this player's stats.
     */
    public void addHit() {
        this.hits++;
    }

    /**
     * Returns the amount of bombers this player has hit as a survivor.
     *
     * @return the hits
     */
    public int getHits() {
        return this.hits;
    }

    @Override
    public void updateNewViewer(Player viewer) {
        switch (this.disguiseState) {
            case NONE -> super.updateNewViewer(viewer);
            case HIDDEN -> {
                // Viewers must not see anything
            }
            case TNT_BLOCK, PRIMED_TNT -> {
                viewer.sendPacket(getSpawnPacket());
                viewer.sendPacket(createDisguiseMetadataPacket());
            }
        }
    }

    @Override
    protected SpawnEntityPacket getSpawnPacket() {
        return switch (this.disguiseState) {
            case TNT_BLOCK -> createDisguiseSpawnPacket(EntityType.FALLING_BLOCK, Block.TNT.stateId());
            case PRIMED_TNT -> createDisguiseSpawnPacket(EntityType.TNT, 0);
            case NONE, HIDDEN -> super.getSpawnPacket();
        };
    }

    /**
     * Every viewer-bound packet of this player passes through here (movement packets excepted, which Minestom
     * sends separately and which the disguise is meant to receive).
     * While disguised, player-only packets would not match the disguise entity's schema on the client,
     * so they are dropped and player metadata is replaced by the disguise metadata.
     */
    @Override
    public void sendPacketToViewers(SendablePacket packet) {
        switch (this.disguiseState) {
            case NONE -> super.sendPacketToViewers(packet);
            case HIDDEN -> {
                // Viewers must not receive anything about this player
            }
            case TNT_BLOCK, PRIMED_TNT -> {
                if (packet instanceof EntityMetaDataPacket) {
                    super.sendPacketToViewers(createDisguiseMetadataPacket());
                    return;
                }
                if (isPlayerOnlyPacket(packet)) return;
                super.sendPacketToViewers(packet);
            }
        }
    }

    /**
     * Minestom uses this on respawn to re-show the player directly on each viewer's connection,
     * bypassing {@link #sendPacketToViewers(SendablePacket)}.
     */
    @Override
    protected void showPlayer(PlayerConnection connection) {
        switch (this.disguiseState) {
            case NONE -> super.showPlayer(connection);
            case HIDDEN -> {
                // Viewers must not see anything
            }
            case TNT_BLOCK, PRIMED_TNT -> {
                connection.sendPacket(getSpawnPacket());
                connection.sendPacket(createDisguiseMetadataPacket());
            }
        }
    }

    private static boolean isPlayerOnlyPacket(SendablePacket packet) {
        return packet instanceof EntityEquipmentPacket
                || packet instanceof EntityAttributesPacket
                || packet instanceof EntityAnimationPacket
                || packet instanceof EntityStatusPacket;
    }

    private SpawnEntityPacket createDisguiseSpawnPacket(EntityType type, int data) {
        Pos position = getPosition();
        return new SpawnEntityPacket(getEntityId(), getUuid(), type, position, 0f, data, Vec.ZERO);
    }

    /**
     * Creates the only metadata viewers may see while this player is disguised.
     * Without the no-gravity flag the client would let the falling block / primed TNT drop between movement packets.
     *
     * @return the metadata packet for the current disguise
     */
    EntityMetaDataPacket createDisguiseMetadataPacket() {
        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>();
        entries.put(MetadataDef.HAS_NO_GRAVITY.index(), Metadata.Boolean(true));
        if (this.disguiseState == DisguiseState.PRIMED_TNT) {
            entries.put(MetadataDef.PrimedTnt.FUSE_TIME.index(), Metadata.VarInt(PRIMED_FUSE_TIME));
        }
        return new EntityMetaDataPacket(getEntityId(), entries);
    }
}
