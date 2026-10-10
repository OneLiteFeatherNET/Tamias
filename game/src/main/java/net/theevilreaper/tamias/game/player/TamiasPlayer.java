package net.theevilreaper.tamias.game.player;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
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

    static final int PRIMED_FUSE_TIME = 50;
    static final BoundingBox DISGUISE_BOUNDING_BOX = new BoundingBox(0.98, 0.98, 0.98);

    private final BoundingBox playerBoundingBox;
    private DisguiseState disguiseState = DisguiseState.NONE;

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
