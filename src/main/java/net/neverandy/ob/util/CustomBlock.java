package net.neverandy.ob.util;

import net.minecraft.block.Block;

/**
 * Created by awweaver on 9/13/16.
 */
public class CustomBlock
{
    public Block block;
    public int x, y, z;
    public int meta;

    public CustomBlock(Block block, int x, int y, int z, int meta)
    {
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
        this.meta = meta;
    }
}
