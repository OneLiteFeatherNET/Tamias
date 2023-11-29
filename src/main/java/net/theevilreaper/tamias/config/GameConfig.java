package net.theevilreaper.tamias.config;

import net.minestom.server.instance.block.Block;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class GameConfig {

    public static final Block SPAWN_BLOCK = Block.TNT;
    public static final int MAX_PLAYERS = 16;
    public static final int MIN_PLAYERS = 5;
    public static final int LOBBY_PHASE_TIME = 30;
    public static final int FORCE_START_TIME = 11;
    public static final int GAME_ROUNDS = 3;
    public static final byte SURVIVOR_ID = 0x00;
    public static final byte TNT_ID = 0x01;
    public static final String MAP_PATH_NAME = "maps";

    private GameConfig() { }
}
