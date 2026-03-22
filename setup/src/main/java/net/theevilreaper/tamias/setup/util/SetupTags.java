package net.theevilreaper.tamias.setup.util;

import net.minestom.server.tag.Tag;

public class SetupTags {

    public static final Tag<Integer> SETUP_TAG = Tag.Transient("suicide.setup");
    public static final Tag<Integer> AUTHOR_AMOUNT_TAG = Tag.Transient("suicide.author_amount");
    public static final Tag<Boolean> DELETE_TAG = Tag.Boolean("delete").defaultValue(false);

    private SetupTags() {

    }
}
