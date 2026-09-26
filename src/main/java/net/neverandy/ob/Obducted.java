package net.neverandy.ob;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.creativetab.CreativeTabs;
import net.neverandy.ob.proxy.CommonProxy;
import net.neverandy.ob.reference.Reference;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Created by awweaver on 4/26/17.
 */
@Mod(name = Reference.MOD_NAME, version = Tags.VERSION, modid = Reference.MOD_ID, acceptedMinecraftVersions = "[1.7.10]")
public class Obducted
{
    @Mod.Instance(Reference.MOD_ID)
    public static Obducted instance;

    public static CreativeTabs tab = new Tab("Items");

    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY, modId = Reference.MOD_ID)
    public static CommonProxy proxy;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
        logger.log(Level.INFO, "Pre Initialization Complete.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        logger.log(Level.INFO, "Initialization Complete.");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
        logger.log(Level.INFO, "Post Initialization Complete.");
    }
}
