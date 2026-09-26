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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.neverandy.ob.blocks.tiles.TileEntitySeedSwapper;

import javax.annotation.Nullable;
import java.util.*;

import static net.neverandy.ob.Obducted.logger;
import static net.neverandy.ob.Obducted.tab;

/**
 * Created by awweaver on 7/15/17.
 */
public class SeedSwapper extends Block
{
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
        return new TileEntitySeedSwapper();
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (worldIn.isRemote)
        {
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEntitySeedSwapper))
        {
            return;
        }

        int sourceDim = worldIn.provider.getDimension();
        Integer chosenDim = pickTargetDim(worldIn);
        WorldServer targetDim = chosenDim == null ? null : worldIn.getMinecraftServer().getWorld(chosenDim);
        if (targetDim == null)
        {
            logger.warn("Seed swapper placed in dim " + sourceDim + " but no other dimension could be loaded");
            if (placer instanceof EntityPlayer)
            {
                placer.sendMessage(new TextComponentString("The swapper couldn't find another dimension to link to."));
            }
            return;
        }

        //Each placed swapper remembers its own target on its tile entity
        ((TileEntitySeedSwapper) te).setData(chosenDim, TileEntitySeedSwapper.DEFAULT_RADIUS);

        //Put a partner swapper in the target dimension that points back here
        if (!(targetDim.getBlockState(pos).getBlock() instanceof SeedSwapper))
        {
            targetDim.setBlockState(pos, getDefaultState());
        }
        TileEntity partner = targetDim.getTileEntity(pos);
        if (partner instanceof TileEntitySeedSwapper)
        {
            ((TileEntitySeedSwapper) partner).setData(sourceDim, TileEntitySeedSwapper.DEFAULT_RADIUS);
            logger.info("Linked seed swapper at " + pos + " between dim " + sourceDim + " and dim " + chosenDim);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        //Only react once per click, not once per hand
        if (worldIn.isRemote || hand != EnumHand.MAIN_HAND)
        {
            return true;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntitySeedSwapper)
        {
            return ((TileEntitySeedSwapper) te).onBlockActivated(worldIn, pos, playerIn);
        }
        return true;
    }

    public Item getItemDropped(int i, Random random, int j)
    {
        return Item.getItemFromBlock(OBBlock.seedSwapper);
    }

    /**
     * Picks a random registered dimension other than the one the world is in, or null if there isn't one.
     */
    @Nullable
    private static Integer pickTargetDim(World world)
    {
        int dimension = world.provider.getDimension();
        List<Integer> dimensions = new ArrayList<>();
        for (int id : DimensionManager.getStaticDimensionIDs())
        {
            if (id != dimension)
            {
                dimensions.add(id);
            }
        }
        if (dimensions.isEmpty())
        {
            return null;
        }
        return dimensions.get(world.rand.nextInt(dimensions.size()));
    }

}
