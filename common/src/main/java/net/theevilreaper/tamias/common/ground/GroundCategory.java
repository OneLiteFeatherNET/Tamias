package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;

import java.util.List;

/**
 * Groups the blocks which can be selected for a {@link GroundData} during the setup.
 * Every block can be used as ground or as decoration block.
 * The blocks must be solid with a full top face, and the ground marker or tnt must not be part of a category.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see GroundData
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public enum GroundCategory {

    STONE("Stone", Block.STONE, List.of(
            Block.STONE, Block.SMOOTH_STONE, Block.COBBLESTONE, Block.MOSSY_COBBLESTONE,
            Block.STONE_BRICKS, Block.MOSSY_STONE_BRICKS, Block.CRACKED_STONE_BRICKS, Block.BRICKS,
            Block.ANDESITE, Block.POLISHED_ANDESITE, Block.DIORITE, Block.POLISHED_DIORITE,
            Block.GRANITE, Block.POLISHED_GRANITE, Block.DEEPSLATE, Block.COBBLED_DEEPSLATE,
            Block.POLISHED_DEEPSLATE, Block.DEEPSLATE_BRICKS, Block.DEEPSLATE_TILES, Block.TUFF,
            Block.POLISHED_TUFF, Block.TUFF_BRICKS, Block.CALCITE, Block.DRIPSTONE_BLOCK
    )),
    WOOD("Wood", Block.OAK_PLANKS, List.of(
            Block.OAK_PLANKS, Block.SPRUCE_PLANKS, Block.BIRCH_PLANKS, Block.JUNGLE_PLANKS,
            Block.ACACIA_PLANKS, Block.DARK_OAK_PLANKS, Block.MANGROVE_PLANKS, Block.CHERRY_PLANKS,
            Block.PALE_OAK_PLANKS, Block.BAMBOO_PLANKS, Block.CRIMSON_PLANKS, Block.WARPED_PLANKS,
            Block.OAK_LOG, Block.SPRUCE_LOG, Block.BIRCH_LOG, Block.JUNGLE_LOG,
            Block.ACACIA_LOG, Block.DARK_OAK_LOG, Block.MANGROVE_LOG, Block.CHERRY_LOG,
            Block.PALE_OAK_LOG, Block.BAMBOO_BLOCK, Block.CRIMSON_STEM, Block.WARPED_STEM
    )),
    WOOL("Wool", Block.WHITE_WOOL, List.of(
            Block.WHITE_WOOL, Block.LIGHT_GRAY_WOOL, Block.GRAY_WOOL, Block.BLACK_WOOL,
            Block.BROWN_WOOL, Block.RED_WOOL, Block.ORANGE_WOOL, Block.YELLOW_WOOL,
            Block.LIME_WOOL, Block.GREEN_WOOL, Block.CYAN_WOOL, Block.LIGHT_BLUE_WOOL,
            Block.BLUE_WOOL, Block.PURPLE_WOOL, Block.MAGENTA_WOOL, Block.PINK_WOOL
    )),
    CONCRETE("Concrete", Block.LIGHT_BLUE_CONCRETE, List.of(
            Block.WHITE_CONCRETE, Block.LIGHT_GRAY_CONCRETE, Block.GRAY_CONCRETE, Block.BLACK_CONCRETE,
            Block.BROWN_CONCRETE, Block.RED_CONCRETE, Block.ORANGE_CONCRETE, Block.YELLOW_CONCRETE,
            Block.LIME_CONCRETE, Block.GREEN_CONCRETE, Block.CYAN_CONCRETE, Block.LIGHT_BLUE_CONCRETE,
            Block.BLUE_CONCRETE, Block.PURPLE_CONCRETE, Block.MAGENTA_CONCRETE, Block.PINK_CONCRETE
    )),
    TERRACOTTA("Terracotta", Block.ORANGE_TERRACOTTA, List.of(
            Block.TERRACOTTA, Block.WHITE_TERRACOTTA, Block.LIGHT_GRAY_TERRACOTTA, Block.GRAY_TERRACOTTA,
            Block.BLACK_TERRACOTTA, Block.BROWN_TERRACOTTA, Block.RED_TERRACOTTA, Block.ORANGE_TERRACOTTA,
            Block.YELLOW_TERRACOTTA, Block.LIME_TERRACOTTA, Block.GREEN_TERRACOTTA, Block.CYAN_TERRACOTTA,
            Block.LIGHT_BLUE_TERRACOTTA, Block.BLUE_TERRACOTTA, Block.PURPLE_TERRACOTTA, Block.MAGENTA_TERRACOTTA,
            Block.PINK_TERRACOTTA
    )),
    SAND("Sand", Block.SAND, List.of(
            Block.SAND, Block.SANDSTONE, Block.SMOOTH_SANDSTONE, Block.CUT_SANDSTONE,
            Block.CHISELED_SANDSTONE, Block.RED_SAND, Block.RED_SANDSTONE, Block.SMOOTH_RED_SANDSTONE,
            Block.CUT_RED_SANDSTONE, Block.CHISELED_RED_SANDSTONE, Block.GRAVEL
    )),
    ICE_AND_SNOW("Ice & Snow", Block.PACKED_ICE, List.of(
            Block.ICE, Block.PACKED_ICE, Block.SNOW_BLOCK
    )),
    NATURE("Nature", Block.GRASS_BLOCK, List.of(
            Block.GRASS_BLOCK, Block.DIRT, Block.COARSE_DIRT, Block.ROOTED_DIRT,
            Block.PODZOL, Block.MYCELIUM, Block.MOSS_BLOCK, Block.PALE_MOSS_BLOCK,
            Block.CLAY, Block.PACKED_MUD, Block.MUD_BRICKS, Block.HAY_BLOCK,
            Block.MELON, Block.PUMPKIN
    )),
    OCEAN("Ocean", Block.PRISMARINE, List.of(
            Block.PRISMARINE, Block.PRISMARINE_BRICKS, Block.DARK_PRISMARINE, Block.SEA_LANTERN
    )),
    NETHER("Nether", Block.NETHERRACK, List.of(
            Block.NETHERRACK, Block.NETHER_BRICKS, Block.RED_NETHER_BRICKS, Block.CRIMSON_NYLIUM,
            Block.WARPED_NYLIUM, Block.SOUL_SOIL, Block.BASALT, Block.POLISHED_BASALT,
            Block.SMOOTH_BASALT, Block.BLACKSTONE, Block.POLISHED_BLACKSTONE, Block.POLISHED_BLACKSTONE_BRICKS,
            Block.GILDED_BLACKSTONE, Block.NETHER_WART_BLOCK, Block.WARPED_WART_BLOCK, Block.SHROOMLIGHT,
            Block.GLOWSTONE, Block.QUARTZ_BLOCK, Block.SMOOTH_QUARTZ, Block.QUARTZ_BRICKS,
            Block.QUARTZ_PILLAR
    )),
    END("End", Block.END_STONE, List.of(
            Block.END_STONE, Block.END_STONE_BRICKS, Block.PURPUR_BLOCK, Block.PURPUR_PILLAR,
            Block.OBSIDIAN, Block.CRYING_OBSIDIAN
    )),
    ORES("Ores & Minerals", Block.DIAMOND_ORE, List.of(
            Block.COAL_ORE, Block.IRON_ORE, Block.COPPER_ORE, Block.GOLD_ORE,
            Block.REDSTONE_ORE, Block.LAPIS_ORE, Block.DIAMOND_ORE, Block.EMERALD_ORE,
            Block.DEEPSLATE_COAL_ORE, Block.DEEPSLATE_IRON_ORE, Block.DEEPSLATE_COPPER_ORE, Block.DEEPSLATE_GOLD_ORE,
            Block.DEEPSLATE_REDSTONE_ORE, Block.DEEPSLATE_LAPIS_ORE, Block.DEEPSLATE_DIAMOND_ORE, Block.DEEPSLATE_EMERALD_ORE,
            Block.COAL_BLOCK, Block.IRON_BLOCK, Block.COPPER_BLOCK, Block.GOLD_BLOCK,
            Block.REDSTONE_BLOCK, Block.LAPIS_BLOCK, Block.DIAMOND_BLOCK, Block.EMERALD_BLOCK
    ));

    /**
     * The maximum amount of blocks a category can contain.
     */
    public static final int MAX_SIZE = 28;

    private final String displayName;
    private final Block icon;
    private final List<Block> blocks;

    GroundCategory(String displayName, Block icon, List<Block> blocks) {
        this.displayName = displayName;
        this.icon = icon;
        this.blocks = blocks;
    }

    /**
     * Returns the name which is shown to the player.
     *
     * @return the display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the block which represents the category.
     *
     * @return the icon block
     */
    public Block icon() {
        return icon;
    }

    /**
     * Returns all blocks of the category.
     *
     * @return the blocks
     */
    public List<Block> blocks() {
        return blocks;
    }
}
