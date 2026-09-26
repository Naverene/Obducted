package net.neverandy.ob.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.config.Configuration;
import net.neverandy.ob.blocks.OBBlock;
import net.neverandy.ob.blocks.tiles.TileEntitySeedSwapper;
import net.neverandy.ob.config.Config;
import net.neverandy.ob.reference.Reference;

/**
 * Created by awweaver on 7/15/17.
 */
public class CommonProxy implements IProxy
{
    public static Configuration config;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        config = new Configuration(event.getSuggestedConfigurationFile());
        Config.readConfig();

        //1.7.10 has no registry events, blocks are registered during preInit
        OBBlock.init();
        GameRegistry.registerTileEntity(TileEntitySeedSwapper.class, Reference.MOD_ID + "_seedswapper");
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
}
