package net.neverandy.ob;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/**
 * Created by andrewweaver on 9/13/16.
 */

public class Tab extends CreativeTabs
{
    private Item tabIconItem;
    private String tabLabel;

    public Tab(String tabID)
    {
        super(CreativeTabs.getNextID(), "ObductedTab" + tabID);
        tabIconItem = Items.diamond;
        tabLabel = "Obducted " + tabID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem()
    {
        return tabIconItem;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel()
    {
        return tabLabel;
    }

    public void setTabIconItem(Item tabItem)
    {
        tabIconItem = tabItem;
    }
}
