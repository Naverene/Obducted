package net.neverandy.ob.blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.neverandy.ob.reference.Reference;

/**
 * Created by awweaver on 7/15/17.
 */
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class OBBlock
{

    @GameRegistry.ObjectHolder("seedswapper")
    public static final SeedSwapper seedSwapper = null;

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        seedSwapper.initModel();
    }
}
