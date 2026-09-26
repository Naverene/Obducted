package net.neverandy.ob.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

/**
 * Places an entity at an exact position in the target dimension.
 * <p>
 * Passed to Forge's {@link Entity#changeDimension(int, ITeleporter)}, which handles all of the
 * world/chunk/client bookkeeping for a dimension change. Because this is not the vanilla
 * teleporter, no nether portal is built and no End platform is generated, and the
 * nether's 8x coordinate scaling is overridden.
 */
public class SwapTeleporter implements ITeleporter
{
    private final double x;
    private final double y;
    private final double z;

    public SwapTeleporter(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw)
    {
        //Forge sends the final position to the client after this, for players too
        entity.motionX = entity.motionY = entity.motionZ = 0;
        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
    }

    public static Entity teleport(Entity entity, int dimension, double x, double y, double z)
    {
        if (entity.world.provider.getDimension() == dimension)
        {
            entity.setPositionAndUpdate(x, y, z);
            return entity;
        }
        return entity.changeDimension(dimension, new SwapTeleporter(x, y, z));
    }
}
