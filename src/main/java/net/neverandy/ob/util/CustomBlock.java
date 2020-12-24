package net.neverandy.ob.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Created by awweaver on 9/13/16.
 */
public class CustomBlock
{
    public Block block;
    public int x, y, z;
    public int meta;
    public BlockPos pos;
    public IBlockState blockState;

    public CustomBlock(Block block, int x, int y, int z, int meta)
    {
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
        this.meta = meta;
    }

    public CustomBlock(IBlockState blockState, BlockPos pos)
    {
        this.blockState = blockState;
        this.block = blockState.getBlock();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.meta = blockState.getBlock().getMetaFromState(blockState);
        this.pos = pos;
    }
}