package net.neverandy.ob;


import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        tabIconItem = Items.DIAMOND;
        tabLabel = "Obducted " + tabID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getTabIconItem()
    {
        return new ItemStack(tabIconItem);
    }

    public String getTranslatedTabLabel()
    {
        return tabLabel;
    }

    public void setTabIconItem(Item tabItem)
    {
        tabIconItem = tabItem;
    }
}