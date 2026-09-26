package net.neverandy.ob.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neverandy.ob.Obducted;

import static net.neverandy.ob.Obducted.logger;

/**
 * Created by awweaver on 7/15/17.
 */
public class SeedSwapperBlockEntity extends BlockEntity
{
    public static final int DEFAULT_RADIUS = 5;

    //Copy blocks exactly: no neighbour updates, so torches, plants etc. don't pop off while the sphere is half built
    private static final int SWAP_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    @Nullable
    private ResourceKey<Level> target;
    private int radius = DEFAULT_RADIUS;

    public SeedSwapperBlockEntity(BlockPos pos, BlockState state)
    {
        super(Obducted.SEED_SWAPPER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if (target != null)
        {
            tag.putString("targetDim", target.location().toString());
        }
        tag.putInt("radius", radius);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        ResourceLocation dim = tag.contains("targetDim", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("targetDim")) : null;
        target = dim == null ? null : ResourceKey.create(Registries.DIMENSION, dim);
        radius = tag.contains("radius", Tag.TAG_INT) ? tag.getInt("radius") : DEFAULT_RADIUS;
    }

    public void link(ResourceKey<Level> dimension)
    {
        this.target = dimension;
        setChanged();
    }

    @Nullable
    public ResourceKey<Level> getTarget()
    {
        return target;
    }

    public void activate(ServerLevel level, BlockPos pos, Player player)
    {
        //Just as in the game that this idea came from, this requires two things to happen, Move the "lever" then click the block.
        if (!level.hasNeighborSignal(pos))
        {
            player.displayClientMessage(Component.translatable("message.ob.needs_power"), true);
            return;
        }

        if (target == null || target == level.dimension())
        {
            player.displayClientMessage(Component.translatable("message.ob.not_linked"), true);
            return;
        }

        ServerLevel dest = level.getServer().getLevel(target);
        if (dest == null || dest.isOutsideBuildHeight(pos))
        {
            logger.warn("Seed swapper at {} in {} can't reach {}", pos, level.dimension().location(), target.location());
            player.displayClientMessage(Component.translatable("message.ob.target_missing", target.location().toString()), true);
            return;
        }

        //Make sure the partner points back here so the trip can be reversed
        if (dest.getBlockEntity(pos) instanceof SeedSwapperBlockEntity partner)
        {
            partner.link(level.dimension());
        }

        //Grab everything that should come along before anything moves
        Vec3 center = Vec3.atCenterOf(pos);
        List<LivingEntity> passengers = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(radius),
                e -> e != player && !e.isPassenger() && !e.isVehicle() && e.distanceToSqr(center) <= radius * radius);

        //Finish generating both areas first, otherwise world generation (trees, ores, structures) could still run and overwrite the swapped blocks
        prepareArea(level, pos, radius);
        prepareArea(dest, pos, radius);

        List<SwapBlock> startingDim = getSphere(level, dest, pos, radius); //Get sphere from current dimension
        List<SwapBlock> swapDim = getSphere(dest, level, pos, radius); //Get sphere from target dimension

        //Set current dimension blocks to target dim blocks
        setBlocks(swapDim, level);

        //Set target dim blocks to current dim blocks
        setBlocks(startingDim, dest);

        //Teleport the player, then everything else that was inside the sphere
        player.stopRiding();
        teleport(player, dest);
        for (LivingEntity e : passengers)
        {
            if (e.isAlive())
            {
                teleport(e, dest);
            }
        }
    }

    private static void teleport(Entity entity, ServerLevel dest)
    {
        entity.teleportTo(dest, entity.getX(), entity.getY(), entity.getZ(), Set.<RelativeMovement>of(), entity.getYRot(), entity.getXRot());
    }

    /**
     * Loads the chunks the sphere touches plus one chunk around them to full status. Features (trees, ores,
     * structures) from a chunk can spill into its neighbours, so this makes sure none of that is still pending
     * for the blocks we are about to swap.
     */
    private static void prepareArea(ServerLevel level, BlockPos pos, int radius)
    {
        int minCx = SectionPos.blockToSectionCoord(pos.getX() - radius) - 1;
        int maxCx = SectionPos.blockToSectionCoord(pos.getX() + radius) + 1;
        int minCz = SectionPos.blockToSectionCoord(pos.getZ() - radius) - 1;
        int maxCz = SectionPos.blockToSectionCoord(pos.getZ() + radius) + 1;
        for (int cx = minCx; cx <= maxCx; cx++)
        {
            for (int cz = minCz; cz <= maxCz; cz++)
            {
                level.getChunk(cx, cz);
            }
        }
    }

    private static void setBlocks(List<SwapBlock> blocks, ServerLevel level)
    {
        for (SwapBlock block : blocks)
        {
            if (canSwap(level.getBlockState(block.pos()))) //Don't replace a block entity or Bedrock
            {
                level.setBlock(block.pos(), block.state(), SWAP_FLAGS);
            }
        }
    }

    /**
     * Collects the blocks of a sphere in {@code level}, skipping positions outside the build height of either dimension.
     */
    private static List<SwapBlock> getSphere(ServerLevel level, ServerLevel other, BlockPos center, int radius)
    {
        List<SwapBlock> blocks = new ArrayList<>();
        int sqradius = radius * radius;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius)))
        {
            if (pos.distSqr(center) > sqradius || level.isOutsideBuildHeight(pos) || other.isOutsideBuildHeight(pos))
            {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            //Don't store Bedrock or block entities (their inventories etc. would be lost)
            if (canSwap(state))
            {
                blocks.add(new SwapBlock(pos.immutable(), state));
            }
        }
        return blocks;
    }

    private static boolean canSwap(BlockState state)
    {
        return !state.is(Blocks.BEDROCK)
                && !(state.getBlock() instanceof SeedSwapper)
                && !state.hasBlockEntity();
    }

    private record SwapBlock(BlockPos pos, BlockState state)
    {
    }
}
