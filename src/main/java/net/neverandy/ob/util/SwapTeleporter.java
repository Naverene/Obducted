package net.neverandy.ob.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

/**
 * Moves entities between dimensions to an exact position.
 * <p>
 * Passed to the server's transferPlayerToDimension, which does all of the world/chunk/client
 * bookkeeping for a dimension change. Overriding placeInPortal means no nether portal is
 * built, no End platform is generated, and the nether's 8x coordinate scaling is ignored.
 */
public class SwapTeleporter extends Teleporter
{
    private final double x;
    private final double y;
    private final double z;

    public SwapTeleporter(WorldServer world, double x, double y, double z)
    {
        super(world);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void placeInPortal(Entity entity, double posX, double posY, double posZ, float yaw)
    {
        entity.motionX = entity.motionY = entity.motionZ = 0;
        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, double posX, double posY, double posZ, float yaw)
    {
        placeInPortal(entity, posX, posY, posZ, yaw);
        return true;
    }

    @Override
    public boolean makePortal(Entity entity)
    {
        return true;
    }

    /**
     * @return the entity in the new dimension. Non-player entities are replaced by a copy, so use the returned one.
     */
    public static Entity teleport(Entity entity, int dimension, double x, double y, double z)
    {
        if (entity.dimension == dimension)
        {
            if (entity instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            else
            {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            return entity;
        }

        MinecraftServer server = MinecraftServer.getServer();
        WorldServer target = server.worldServerForDimension(dimension);
        if (target == null)
        {
            return entity;
        }

        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            int oldDim = player.dimension;
            server.getConfigurationManager().transferPlayerToDimension(player, dimension, new SwapTeleporter(target, x, y, z));
            if (oldDim == 1)
            {
                //Vanilla only leaves the End through the respawn screen, so the transfer skips spawning the player into the new world
                player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
                target.spawnEntityInWorld(player);
                target.updateEntityWithOptionalForce(player, false);
            }
            player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
            //The client shows an empty XP bar after a dimension change until this is resent
            player.addExperienceLevel(0);
            return player;
        }

        //Same approach as Entity.travelToDimension: copy the entity into the new world and kill the old one
        Entity copy = EntityList.createEntityByName(EntityList.getEntityString(entity), target);
        if (copy == null)
        {
            return entity;
        }
        WorldServer source = (WorldServer) entity.worldObj;
        copy.copyDataFrom(entity, true);
        source.removeEntity(entity);
        copy.dimension = dimension;
        copy.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        boolean forceSpawn = copy.forceSpawn;
        copy.forceSpawn = true;
        target.spawnEntityInWorld(copy);
        copy.forceSpawn = forceSpawn;
        source.resetUpdateEntityTick();
        target.resetUpdateEntityTick();
        return copy;
    }
}
