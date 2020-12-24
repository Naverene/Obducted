package net.neverandy.ob.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.neverandy.ob.blocks.OBBlock;

/**
 * Created by awweaver on 7/15/17.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        OBBlock.initModels();
        //OBItem.initModels();
    }
}