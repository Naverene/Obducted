package net.neverandy.ob.blocks.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.neverandy.ob.blocks.SeedSwapper;
import net.neverandy.ob.util.CustomBlock;
import net.neverandy.ob.util.SwapTeleporter;

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
        this.linked = nbt.getBoolean("linked");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger("targetDim", this.chosenDim);
        nbt.setInteger("radius", this.radius);
        nbt.setBoolean("linked", this.linked);
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

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player)
    {
        if (world.isRemote)
        {
            return true;
        }

        //Just as in the game that this idea came from, this requires two things to happen, Move the "lever" then click the block.
        if (!world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            player.addChatMessage(new ChatComponentText("The swapper needs redstone power."));
            return true;
        }

        int sourceDim = world.provider.dimensionId;
        if (!linked || chosenDim == sourceDim || !DimensionManager.isDimensionRegistered(chosenDim))
        {
            logger.warn("Seed swapper at " + x + ", " + y + ", " + z + " in dim " + sourceDim + " has no valid target (target: " + chosenDim + ", linked: " + linked + ")");
            player.addChatMessage(new ChatComponentText("This swapper isn't linked to another dimension. Break it and place it again."));
            return true;
        }

        //Loads the target dimension if nobody is in it (DimensionManager.getWorld would just return null)
        WorldServer dest = MinecraftServer.getServer().worldServerForDimension(chosenDim);
        if (dest == null)
        {
            player.addChatMessage(new ChatComponentText("Dimension " + chosenDim + " could not be loaded."));
            return true;
        }

        //Make sure the partner points back here so the trip can be reversed
        TileEntity partner = dest.getTileEntity(x, y, z);
        if (partner instanceof TileEntitySeedSwapper)
        {
            ((TileEntitySeedSwapper) partner).setData(sourceDim, this.radius);
        }

        //Grab everything that should come along before anything moves
        double cx = x + 0.5, cy = y + 0.5, cz = z + 0.5;
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(cx - radius, cy - radius, cz - radius, cx + radius, cy + radius, cz + radius);
        List<EntityLivingBase> passengers = new ArrayList<>();
        for (Object o : world.getEntitiesWithinAABB(EntityLivingBase.class, area))
        {
            EntityLivingBase e = (EntityLivingBase) o;
            if (e != player && e.ridingEntity == null && e.riddenByEntity == null && e.getDistanceSq(cx, cy, cz) <= radius * radius)
            {
                passengers.add(e);
            }
        }

        List<CustomBlock> startingDim = getSphere(world, x, y, z, this.radius); //Get sphere from current dimension
        List<CustomBlock> swapDim = getSphere(dest, x, y, z, this.radius); //Get sphere from target dimension

        //Set current dimension blocks to target dim blocks
        setBlocks(swapDim, world);

        //Set target dim blocks to current dim blocks
        setBlocks(startingDim, dest);

        //Teleport the player, then everything else that was inside the sphere
        if (player.ridingEntity != null)
        {
            player.mountEntity(null);
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

    private void setBlocks(List<CustomBlock> blocks, World world)
    {
        for (CustomBlock cb : blocks)
        {
            if (!canSwap(world.getBlock(cb.x, cb.y, cb.z), world.getBlockMetadata(cb.x, cb.y, cb.z))) //Don't replace a TileEntity or Bedrock
            {
                continue;
            }
            world.setBlock(cb.x, cb.y, cb.z, cb.block, cb.meta, 3);
        }
    }

    private List<CustomBlock> getSphere(World world, int centerx, int centery, int centerz, int radius)
    {
        List<CustomBlock> blockArrayList = new ArrayList<>();
        int sqradius = radius * radius;

        for (int x = centerx - radius; x <= centerx + radius; x++)
        {
            int dxdx = (x - centerx) * (x - centerx);
            for (int z = centerz - radius; z <= centerz + radius; z++)
            {
                int dzdz = (z - centerz) * (z - centerz);
                for (int y = Math.max(1, centery - radius); y <= Math.min(world.getHeight() - 1, centery + radius); y++)
                {
                    int dydy = (y - centery) * (y - centery);
                    if (dxdx + dydy + dzdz > sqradius)
                    {
                        continue;
                    }
                    Block block = world.getBlock(x, y, z);
                    int meta = world.getBlockMetadata(x, y, z);
                    //Don't store Bedrock or TileEntity (their inventories etc. would be lost)
                    if (canSwap(block, meta))
                    {
                        blockArrayList.add(new CustomBlock(block, x, y, z, meta));
                    }
                }
            }
        }
        return blockArrayList;
    }

    private static boolean canSwap(Block block, int meta)
    {
        return block != Blocks.bedrock
                && !(block instanceof SeedSwapper)
                && !block.hasTileEntity(meta);
    }
}
