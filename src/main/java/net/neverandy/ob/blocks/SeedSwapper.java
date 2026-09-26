package net.neverandy.ob.blocks;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.neverandy.ob.blocks.tiles.TileEntitySeedSwapper;
import net.neverandy.ob.reference.Reference;

import static net.neverandy.ob.Obducted.logger;
import static net.neverandy.ob.Obducted.tab;

/**
 * Created by awweaver on 7/15/17.
 */
public class SeedSwapper extends Block
{
    //Metadata holds which side the front faces, same numbering as the furnace (2-5)
    private static final int[] FACING_BY_ROTATION = {2, 5, 3, 4};

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;

    public SeedSwapper(Material blockMaterialIn)
    {
        super(blockMaterialIn);
        setCreativeTab(tab);

        setHardness(5.0F);
        setResistance(2000.0F);
        setHarvestLevel("pickaxe", 2);
        setStepSound(Block.soundTypeAnvil);
        setBlockName("seedswapper");
        setBlockTextureName(Reference.MOD_ID + ":seedswapper/seedswapperside");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register)
    {
        String base = Reference.MOD_ID + ":seedswapper/seedswapper";
        blockIcon = register.registerIcon(base + "side");
        iconTop = register.registerIcon(base + "top");
        iconBottom = register.registerIcon(base + "bottom");
        iconFront = register.registerIcon(base + "front");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        if (side == 0)
        {
            return iconBottom;
        }
        if (side == 1)
        {
            return iconTop;
        }
        //meta 0 is the item in the inventory, show the front facing the player
        if (side == meta || (meta == 0 && side == 3))
        {
            return iconFront;
        }
        return blockIcon;
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileEntitySeedSwapper();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack stack)
    {
        int rotation = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int facing = FACING_BY_ROTATION[rotation];
        worldIn.setBlockMetadataWithNotify(x, y, z, facing, 2);

        if (worldIn.isRemote)
        {
            return;
        }

        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (!(te instanceof TileEntitySeedSwapper))
        {
            return;
        }

        int sourceDim = worldIn.provider.dimensionId;
        Integer chosenDim = pickTargetDim(worldIn);
        //worldServerForDimension loads the dimension if nobody is in it
        WorldServer targetDim = chosenDim == null ? null : MinecraftServer.getServer().worldServerForDimension(chosenDim);
        if (targetDim == null)
        {
            logger.warn("Seed swapper placed in dim " + sourceDim + " but no other dimension could be loaded");
            if (placer instanceof EntityPlayer)
            {
                ((EntityPlayer) placer).addChatMessage(new ChatComponentText("The swapper couldn't find another dimension to link to."));
            }
            return;
        }

        //Each placed swapper remembers its own target on its tile entity
        ((TileEntitySeedSwapper) te).setData(chosenDim, TileEntitySeedSwapper.DEFAULT_RADIUS);

        //Put a partner swapper in the target dimension that points back here
        if (!(targetDim.getBlock(x, y, z) instanceof SeedSwapper))
        {
            targetDim.setBlock(x, y, z, this, facing, 3);
        }
        TileEntity partner = targetDim.getTileEntity(x, y, z);
        if (partner instanceof TileEntitySeedSwapper)
        {
            ((TileEntitySeedSwapper) partner).setData(sourceDim, TileEntitySeedSwapper.DEFAULT_RADIUS);
            logger.info("Linked seed swapper at " + x + ", " + y + ", " + z + " between dim " + sourceDim + " and dim " + chosenDim);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer playerIn, int side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te instanceof TileEntitySeedSwapper)
        {
            return ((TileEntitySeedSwapper) te).onBlockActivated(worldIn, x, y, z, playerIn);
        }
        return true;
    }

    /**
     * Picks a random registered dimension other than the one the world is in, or null if there isn't one.
     */
    private static Integer pickTargetDim(World world)
    {
        int dimension = world.provider.dimensionId;
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
