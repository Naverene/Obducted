package net.neverandy.ob.blocks.tiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.neverandy.ob.blocks.SeedSwapper;
import net.neverandy.ob.util.CustomBlock;
import net.neverandy.ob.util.SwapTeleporter;

import java.util.ArrayList;
import java.util.List;

import static net.neverandy.ob.Obducted.logger;

/**
 * Created by awweaver on 7/15/17.
 */
public class TileEntitySeedSwapper extends TileEntity
{
    public static final int DEFAULT_RADIUS = 5;

    private int chosenDim;
    private int radius = DEFAULT_RADIUS;
    // Without this a freshly created swapper would look like it points at dimension 0
    private boolean linked = false;

    public TileEntitySeedSwapper() //Complains if this isn't here
    {
        //Nothing goes here
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        this.radius = nbt.hasKey("radius") ? nbt.getInteger("radius") : DEFAULT_RADIUS;
        this.chosenDim = nbt.getInteger("targetDim");
        // Swappers saved before the "linked" flag existed still have a target
        this.linked = nbt.hasKey("linked") ? nbt.getBoolean("linked") : nbt.hasKey("targetDim");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger("targetDim", this.chosenDim);
        nbt.setInteger("radius", this.radius);
        nbt.setBoolean("linked", this.linked);

        return nbt;
    }

    public void setData(int dimID, int radius)
    {
        this.chosenDim = dimID;
        this.radius = radius;
        this.linked = true;
        logger.debug("TE setData, chosenDim: " + this.chosenDim + " radius: " + radius);
        this.markDirty();
    }

    public boolean isLinked()
    {
        return linked;
    }

    public int getChosenDim()
    {
        return chosenDim;
    }

    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer player)
    {
        if (world.isRemote)
        {
            return true;
        }

        //Just as in the game that this idea came from, this requires two things to happen, Move the "lever" then click the block.
        if (!world.isBlockPowered(pos))
        {
            player.sendMessage(new TextComponentString("The swapper needs redstone power."));
            return true;
        }

        int sourceDim = world.provider.getDimension();
        if (!linked || chosenDim == sourceDim || !DimensionManager.isDimensionRegistered(chosenDim))
        {
            logger.warn("Seed swapper at " + pos + " in dim " + sourceDim + " has no valid target (target: " + chosenDim + ", linked: " + linked + ")");
            player.sendMessage(new TextComponentString("This swapper isn't linked to another dimension. Break it and place it again."));
            return true;
        }

        //Loads the target dimension if nobody is in it (DimensionManager.getWorld would just return null)
        WorldServer dest = world.getMinecraftServer().getWorld(chosenDim);
        if (dest == null)
        {
            player.sendMessage(new TextComponentString("Dimension " + chosenDim + " could not be loaded."));
            return true;
        }

        //Make sure the partner points back here so the trip can be reversed
        TileEntity partner = dest.getTileEntity(pos);
        if (partner instanceof TileEntitySeedSwapper)
        {
            ((TileEntitySeedSwapper) partner).setData(sourceDim, this.radius);
        }

        //Grab everything that should come along before anything moves
        AxisAlignedBB area = new AxisAlignedBB(pos).grow(this.radius);
        List<EntityLivingBase> passengers = new ArrayList<>();
        for (EntityLivingBase e : world.getEntitiesWithinAABB(EntityLivingBase.class, area))
        {
            if (e != player && !e.isRiding() && !e.isBeingRidden() && e.getDistanceSq(pos) <= this.radius * this.radius)
            {
                passengers.add(e);
            }
        }

        //Finish generating both areas first, otherwise world generation (trees, ores, structures) runs later and overwrites the swapped blocks
        prepareArea((WorldServer) world, pos, this.radius);
        prepareArea(dest, pos, this.radius);

        ArrayList<CustomBlock> startingDim = getSphere(world, pos, this.radius); //Get sphere from current dimension
        ArrayList<CustomBlock> swapDim = getSphere(dest, pos, this.radius); //Get sphere from target dimension

        //Set current dimension blocks to target dim blocks
        setBlocks(swapDim, world);

        //Set target dim blocks to current dim blocks
        setBlocks(startingDim, dest);

        //Teleport the player, then everything else that was inside the sphere
        if (player.isRiding())
        {
            player.dismountRidingEntity();
        }
        SwapTeleporter.teleport(player, chosenDim, player.posX, player.posY, player.posZ);
        for (EntityLivingBase e : passengers)
        {
            if (!e.isDead)
            {
                SwapTeleporter.teleport(e, chosenDim, e.posX, e.posY, e.posZ);
            }
        }
        return true;
    }

    /**
     * Loads the chunks the sphere touches plus one chunk around them. A chunk is only decorated (trees, ores,
     * structures) once its neighbours are loaded, and decoration spills half a chunk into the next one, so this
     * makes sure no decoration is still pending for the blocks we are about to swap.
     */
    private static void prepareArea(WorldServer world, BlockPos pos, int radius)
    {
        int minCx = ((pos.getX() - radius) >> 4) - 1;
        int maxCx = ((pos.getX() + radius) >> 4) + 1;
        int minCz = ((pos.getZ() - radius) >> 4) - 1;
        int maxCz = ((pos.getZ() + radius) >> 4) + 1;
        for (int cx = minCx; cx <= maxCx; cx++)
        {
            for (int cz = minCz; cz <= maxCz; cz++)
            {
                world.getChunkProvider().provideChunk(cx, cz);
            }
        }
    }

    private void setBlocks(ArrayList<CustomBlock> blocks, World world)
    {
        for (CustomBlock cb : blocks)
        {
            IBlockState existing = world.getBlockState(cb.pos);
            if (!canSwap(existing)) //Don't replace a TileEntity or Bedrock
            {
                continue;
            }
            world.setBlockState(cb.pos, cb.blockState);
        }
    }

    private ArrayList<CustomBlock> getSphere(World world, BlockPos center, int radius)
    {
        ArrayList<CustomBlock> blockArrayList = new ArrayList<>();
        int sqradius = radius * radius;

        for (BlockPos.MutableBlockPos p : BlockPos.getAllInBoxMutable(center.add(-radius, -radius, -radius), center.add(radius, radius, radius)))
        {
            if (p.getY() <= 0 || p.getY() >= world.getHeight() || p.distanceSq(center) > sqradius)
            {
                continue;
            }
            IBlockState state = world.getBlockState(p);
            //Don't store Bedrock or TileEntity (their inventories etc. would be lost)
            if (canSwap(state))
            {
                blockArrayList.add(new CustomBlock(state, p.toImmutable()));
            }
        }
        return blockArrayList;
    }

    private static boolean canSwap(IBlockState state)
    {
        return state.getBlock() != Blocks.BEDROCK
                && !(state.getBlock() instanceof SeedSwapper)
                && !state.getBlock().hasTileEntity(state);
    }
}
