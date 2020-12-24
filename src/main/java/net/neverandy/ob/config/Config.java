package net.neverandy.ob.config;

import net.minecraftforge.common.config.Configuration;
import net.neverandy.ob.Obducted;
import net.neverandy.ob.proxy.CommonProxy;
import org.apache.logging.log4j.Level;

/**
 * Created by andrewweaver on 7/15/17.
 */
public class Config
{
    private static final String CATEGORY_GENERAL = "general";
    public static boolean verboseLogging = false;

    public static void readConfig()
    {
        Configuration cfg = CommonProxy.config;
        try
        {
            cfg.load();
            initGeneralConfig(cfg);
        }
        catch (Exception exception)
        {
            Obducted.logger.log(Level.ERROR, "Problem loading config file", exception);
        }
        finally
        {
            if (cfg.hasChanged())
            {
                cfg.save();
            }
        }
    }

    private static void initGeneralConfig(Configuration cfg)
    {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General Configuration");
        verboseLogging = cfg.getBoolean("verboseLogging", CATEGORY_GENERAL, false, "Adds extra stuff to the log to help find where issues are coming from");
    }
}
