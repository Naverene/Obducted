package net.neverandy.ob.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.neverandy.ob.blocks.tiles.TileEntitySeedSwapper;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

import static net.neverandy.ob.Obducted.logger;
import static net.neverandy.ob.Obducted.tab;

/**
 * Created by awweaver on 7/15/17.
 */
public class SeedSwapper extends Block
{
    private int chosenDim;
    private int radius = 5;

    public SeedSwapper(Material blockMaterialIn, MapColor blockMapColorIn)
    {

        super(blockMaterialIn, blockMapColorIn);
        setCreativeTab(tab);

        setHarvestLevel("iron", 2);
        setSoundType(SoundType.ANVIL);
        setRegistryName("seedswapper");
        setUnlocalizedName("seedswapper");
    }

    @SideOnly(Side.CLIENT)
    public void initModel()
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntitySeedSwapper(this.chosenDim);

    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (!worldIn.isRemote)
        {
            getChosenDim(worldIn);
            //this.chosenDim = getChosenDim(worldIn);
            logger.info("onBlockPLaced, ChosenDim: " + this.chosenDim);
            World targetDim = worldIn.getMinecraftServer().getWorld(this.chosenDim);
            if(targetDim == null)
            {
                return;
            }
            IBlockState blockState = targetDim.getBlockState(pos);
            if (blockState == null)
            {
                return;
            }

            if (!(blockState instanceof SeedSwapper))
            {
                logger.info("Original Dimension ID: " + worldIn.provider.getDimension());
                if (targetDim.setBlockState(pos, OBBlock.seedSwapper.getDefaultState()))
                {
                    logger.info("Set partner swapper at X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ() + " dim:  " + this.chosenDim);
                    TileEntitySeedSwapper swapper = new TileEntitySeedSwapper(worldIn.provider.getDimension());
                    targetDim.setTileEntity(pos, swapper);
                    swapper.setData(worldIn.provider.getDimension(), this.radius);
                }
                //TileEntitySeedSwapper te = (TileEntitySeedSwapper) targetDim.getTileEntity(x, y, z);
                logger.info("Chosen Dim: " + this.chosenDim + " Radius: " + this.radius, Level.INFO);
                //te.setData(world.provider.dimensionId, this.radius);
                //Obducted.network.sendToServer(new SeedSwapperMessage("swapper", x, y, z, world.provider.dimensionId, this.chosenDim, this.radius));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            //this.dimID = getChosenDim(worldIn);
            logger.log(Level.INFO, "onBlockActivated, line 185");
            TileEntitySeedSwapper te = (TileEntitySeedSwapper) worldIn.getTileEntity(pos);
            te.setData(this.chosenDim, this.radius);
            logger.log(Level.INFO, "onBlockActivated, ChosenDim: " + this.chosenDim);
            return te.onBlockActivated(worldIn, pos, playerIn, facing, hitX, hitY, hitZ);
        }
        return true;
    }

    public Item getItemDropped(int i, Random random, int j)
    {
        return Item.getItemFromBlock(OBBlock.seedSwapper);
    }

    public void setData(int dimID, int radius)
    {

    }

    public int getChosenDim(World world)
    {
        DimensionType[] dimensionTypes = DimensionType.values();
        int dimension = world.provider.getDimension();
        ArrayList<Integer> dimensions = new ArrayList();

        for (DimensionType dimensionType : dimensionTypes)
        {
            int[] temp=DimensionManager.getDimensions(dimensionType);
            for (int aTemp : temp)
            {
                if(aTemp!=dimension)
                {
                    dimensions.add(aTemp);
                }
            }
        }
        this.chosenDim=dimensions.get(world.rand.nextInt(dimensions.size()));
        logger.info("getChosenDim: " + this.chosenDim);
        return chosenDim;
    }

    public void setChosenDim(int dim)
    {
        this.chosenDim = dim;
        logger.info("Setting Chosen Dim to: " + dim);
    }

}
