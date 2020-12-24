package net.neverandy.ob.blocks.tiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.neverandy.ob.blocks.SeedSwapper;
import net.neverandy.ob.util.CustomBlock;
import net.neverandy.ob.util.Teleporter;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

import static net.neverandy.ob.Obducted.logger;

/**
 * Created by awweaver on 7/15/17.
 */
public class TileEntitySeedSwapper extends TileEntity
{
    private int chosenDim;
    private int radius;

    public TileEntitySeedSwapper() //Complains if this isn't here
    {
        //Nothing goes here
    }

    public TileEntitySeedSwapper(int dimID)
    {
        this.chosenDim = dimID;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        this.radius = nbt.getInteger("radius");
        this.chosenDim = nbt.getInteger("targetDim");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger("targetDim", this.chosenDim);
        nbt.setInteger("radius", this.radius);

        return nbt;
    }

    public void setData(int dimID, int radius)
    {
        if (dimID == Integer.MIN_VALUE)
        {
            World world = Minecraft.getMinecraft().world;
            SeedSwapper swapper = (SeedSwapper) world.getBlockState(pos).getBlock();
            logger.info("Chosen Dim: " + this.chosenDim);
        } else
        {
            this.chosenDim = dimID;
        }

        this.radius = radius;
        logger.info("TE setData, chosenDim: " + this.chosenDim + " dimID: " + dimID);
        this.markDirty();
    }

    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer player, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        Integer[] ids = DimensionManager.getIDs();
        GuiIngame guiIngame;
        if (!world.isRemote)
        {
            guiIngame = new GuiIngame(Minecraft.getMinecraft());
            /*
            for (int i = 0; i < DimensionManager.getIDs().length; i++)
            {
                String dim = "ID: " + ids[i];
                guiIngame.addChatMessage(ChatType.CHAT, new TextComponentString(dim));
                logger.log(Level.INFO, dim);
            }

             */
            ArrayList<Integer> dimensions = new ArrayList();
            DimensionType[] dimensionTypes = DimensionType.values();
            int dimension = world.provider.getDimension();
            for (DimensionType dimensionType : dimensionTypes)
            {
                int[] temp=DimensionManager.getDimensions(dimensionType);
                for (int aTemp : temp)
                {
                    if(aTemp!=dimension)
                    {
                        dimensions.add(aTemp);
                        String dim = "ID: " + aTemp;
                        guiIngame.addChatMessage(ChatType.CHAT, new TextComponentString(dim));
                    }
                }
            }
            logger.info("TE onBlockActivated: " + this.chosenDim, Level.INFO);
            //Destination Dimension world object
            World dest = DimensionManager.getWorld(this.chosenDim);
            //DimensionManager.getWorld(this.chosenDim);
            ArrayList<CustomBlock> startingDim;
            ArrayList<CustomBlock> swapDim;

            //Just as in the game that this idea came from, this requires two things to happen, Move the "lever" then click the block.
            if (hasRedstonePower(this.pos, world))
            {
                logger.info("Sphere");
                startingDim = getSphere(world, pos.getX(), pos.getY(), pos.getZ(), this.radius); //Get sphere from current dimension
                swapDim = getSphere(dest, pos.getX(), pos.getY(), pos.getZ(), this.radius); //Get sphere from target dimension

                //Set current dimension blocks to target dim blocks
                setBlocks(swapDim, world);

                //Set target dim blocks to current dim blocks
                setBlocks(startingDim, dest);

                //Teleport the player
                Teleporter.TeleportLocation loc = new Teleporter.TeleportLocation(player.posX, player.posY, player.posZ, this.chosenDim, player.cameraPitch, player.getRotationYawHead());

                if (this.chosenDim != player.dimension)
                {
                    if (dest.getTileEntity(pos) instanceof TileEntitySeedSwapper)
                    {
                        logger.info("Sent Data to block in the target Dimension");
                        TileEntitySeedSwapper te = (TileEntitySeedSwapper) dest.getTileEntity(pos);
                        SeedSwapper swapper = (SeedSwapper) dest.getBlockState(pos).getBlock();
                        te.setData(player.dimension, this.radius);
                        swapper.setChosenDim(player.dimension);

                    }
                    loc.teleport(player);
                    List list = player.getEntityWorld().getLoadedEntityList();
                    ArrayList<EntityLiving> entitiesToTeleport = new ArrayList<>();
                    for (Object e : list)
                    {
                        if (e instanceof EntityLiving)
                        {
                            logger.info(e + " is instanceof EntityLiving");
                            double distance = Math.sqrt(((EntityLiving) e).getDistanceSq(this.pos));
                            if (distance <= this.radius)
                            {
                                logger.info("Distance: " + distance);
                                entitiesToTeleport.add((EntityLiving) e);
                            }
                        }
                    }
                    for (EntityLiving e : entitiesToTeleport)
                    {
                        logger.info(e);
                        Teleporter.TeleportLocation location = new Teleporter.TeleportLocation(e.posX, e.posY, e.posZ, this.chosenDim, e.cameraPitch, e.getRotationYawHead());
                        location.teleport(e);
                    }
                }
            } else
            {
                return false;
            }

            //Obducted.network.sendToServer(new SeedSwapperMessage("swapper",this.xCoord,this.yCoord,this.zCoord,this.worldObj.provider.dimensionId,this.chosenDim,this.radius));
        }
        return true;
    }

    private boolean hasRedstonePower(BlockPos pos, World world)
    {
        return world.getStrongPower(pos) != 0;
    }

    private void setBlocks(ArrayList<CustomBlock> blocks, World world)
    {
        logger.info("Setting blocks");
        logger.info(world.provider.getDimensionType().getName());
        //for(int i=0;i<blocks.size();i++)
        while (blocks.size() > 0)
        {
            CustomBlock cb = blocks.get(0);
            //FMLLog.log(Level.INFO, "X: %s, Y: %s, Z: %s, Block: %s", cb.x, cb.y, cb.z, cb.block.getUnlocalizedName());
            if (!(world.getBlockState(cb.pos) instanceof TileEntity)) //Don't replace a TileEntity
            {
                world.setBlockState(cb.pos, cb.blockState);
            }
            //world.setBlockMetadataWithNotify(cb.x, cb.y, cb.z, cb.meta, 0);
            blocks.remove(cb);
        }
    }

    private ArrayList<CustomBlock> getSphere(World world, int centerx, int centery, int centerz, int radius)
    {
        logger.info("Running getSphere");
        logger.info(world.provider.getDimension());

        ArrayList<CustomBlock> blockArrayList = new ArrayList<>();
        blockArrayList.clear();
        double sqradius = radius * radius;

        for (int x = centerx - radius; x <= centerx + radius; x++)
        {
            double dxdx = (x - centerx) * (x - centerx);
            for (int z = centerz - radius; z < centerz + radius; z++)
            {
                double dzdz = (z - centerz) * (z - centerz);
                for (int y = centery - radius; y <= centery + radius; y++)
                {
                    double dydy = (y - centery) * (y - centery);

                    double sqdist = dxdx + dydy + dzdz;
                    if (sqdist <= sqradius)
                    {
                        if (y > 0)
                        {
                            String dbg = "blockPos: " + x + "," + y + "," + z;
                            BlockPos blockPos = new BlockPos(x, y, z);
                            logger.info(dbg);
                            dbg = "Block: " + world.getBlockState(blockPos).getBlock().getUnlocalizedName();
                            logger.info(dbg);

                            //Don't store Bedrock or TileEntity
                            if (!(world.getBlockState(blockPos) == Blocks.BEDROCK) || world.getBlockState(blockPos) instanceof TileEntity)
                            {
                                blockArrayList.add(new CustomBlock(world.getBlockState(blockPos), blockPos));
                            }
                        }
                    }
                }
            }
        }
        return blockArrayList;
    }
}

