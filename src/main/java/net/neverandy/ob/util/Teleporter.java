package net.neverandy.ob.util;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by awweaver on 7/16/17.
 */
public class Teleporter
{
    public static class TeleportLocation
    {
        protected double xCoord;
        protected double yCoord;
        protected double zCoord;
        protected int dimension;
        protected float pitch;
        protected float yaw;
        protected String name;
        protected String dimensionName = "";
        protected boolean writeProtected = false;

        public TeleportLocation()
        {

        }

        public TeleportLocation(double x, double y, double z, int dimension)
        {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
            this.dimension = dimension;
            this.pitch = 0;
            this.yaw = 0;
        }

        public TeleportLocation(double x, double y, double z, int dimension, float pitch, float yaw)
        {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
            this.dimension = dimension;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public TeleportLocation(double x, double y, double z, int dimension, float pitch, float yaw, String name)
        {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
            this.dimension = dimension;
            this.pitch = pitch;
            this.yaw = yaw;
            this.name = name;
        }

        public double getXCoord()
        {
            return xCoord;
        }

        public void setXCoord(double x)
        {
            xCoord = x;
        }

        public double getYCoord()
        {
            return yCoord;
        }

        public void setYCoord(double y)
        {
            yCoord = y;
        }

        public double getZCoord()
        {
            return zCoord;
        }

        public void setZCoord(double z)
        {
            zCoord = z;
        }

        public int getDimension()
        {
            return dimension;
        }

        public void setDimension(int d)
        {
            dimension = d;
        }

        public String getDimensionName()
        {
            return dimensionName;
        }

        public void setDimensionName(String dimensionName)
        {
            this.dimensionName = dimensionName;
        }

        public float getPitch()
        {
            return pitch;
        }

        public void setPitch(float p)
        {
            pitch = p;
        }

        public float getYaw()
        {
            return yaw;
        }

        public void setYaw(float y)
        {
            yaw = y;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String s)
        {
            name = s;
        }

        public boolean getWriteProtected()
        {
            return writeProtected;
        }

        public void setWriteProtected(boolean b)
        {
            writeProtected = b;
        }

        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setDouble("X", xCoord);
            compound.setDouble("Y", yCoord);
            compound.setDouble("Z", zCoord);
            compound.setInteger("Dimension", dimension);
            compound.setFloat("Pitch", pitch);
            compound.setFloat("Yaw", yaw);
            compound.setString("Name", name);
            compound.setString("DimentionName", dimensionName);
            compound.setBoolean("WP", writeProtected);
        }

        public void readFromNBT(NBTTagCompound compound)
        {
            xCoord = compound.getDouble("X");
            yCoord = compound.getDouble("Y");
            zCoord = compound.getDouble("Z");
            dimension = compound.getInteger("Dimension");
            pitch = compound.getFloat("Pitch");
            yaw = compound.getFloat("Yaw");
            name = compound.getString("Name");
            dimensionName = compound.getString("DimentionName");
            writeProtected = compound.getBoolean("WP");
        }

        public void teleport(Entity entity)
        {
            TeleportUtils.teleportEntity(entity, dimension, xCoord, yCoord, zCoord, yaw, pitch);
        }

        @Override
        public int hashCode()
        {
            return (xCoord + "-" + yCoord + "-" + zCoord + "-" + name + "-" + dimensionName + "-" + dimension + "-" + yaw + "-" + pitch).hashCode();
        }
    }
}
