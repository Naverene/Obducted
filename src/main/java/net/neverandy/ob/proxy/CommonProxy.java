package net.neverandy.ob.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.neverandy.ob.blocks.OBBlock;
import net.neverandy.ob.blocks.SeedSwapper;
import net.neverandy.ob.blocks.tiles.TileEntitySeedSwapper;
import net.neverandy.ob.config.Config;
import net.neverandy.ob.reference.Reference;

/**
 * Created by awweaver on 7/15/17.
 */
@Mod.EventBusSubscriber
public class
CommonProxy implements IProxy
{
    public static Configuration config;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        config = new Configuration(event.getSuggestedConfigurationFile());
        Config.readConfig();
    }

    @Override
    public void init(FMLInitializationEvent event)
    {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        if (config.hasChanged())
        {
            config.save();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(new SeedSwapper(Material.ANVIL, MapColor.BLUE));
        GameRegistry.registerTileEntity(TileEntitySeedSwapper.class, Reference.MOD_ID + "_seedswapper");

    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();
        assert OBBlock.seedSwapper != null;
        registry.register(new ItemBlock(OBBlock.seedSwapper).setRegistryName(OBBlock.seedSwapper.getRegistryName()));

    }
}