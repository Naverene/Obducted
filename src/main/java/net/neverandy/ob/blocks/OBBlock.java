package net.neverandy.ob.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;

/**
 * Created by awweaver on 7/15/17.
 */
public class OBBlock
{
    public static SeedSwapper seedSwapper;

    public static void init()
    {
        seedSwapper = new SeedSwapper(Material.anvil);
        GameRegistry.registerBlock(seedSwapper, "seedswapper");
    }
}
