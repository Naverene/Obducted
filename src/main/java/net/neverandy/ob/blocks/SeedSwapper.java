package net.neverandy.ob.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import static net.neverandy.ob.Obducted.logger;

/**
 * Created by awweaver on 7/15/17.
 */
public class SeedSwapper extends HorizontalDirectionalBlock implements EntityBlock
{
    public static final MapCodec<SeedSwapper> CODEC = simpleCodec(SeedSwapper::new);

    public SeedSwapper(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        //Front faces the player that placed it
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SeedSwapperBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel serverLevel) || !(level.getBlockEntity(pos) instanceof SeedSwapperBlockEntity swapper))
        {
            return;
        }

        ServerLevel target = pickTargetDim(serverLevel, pos);
        if (target == null)
        {
            logger.warn("Seed swapper placed in {} at {} but no other dimension has room for it", level.dimension().location(), pos);
            if (placer instanceof Player player)
            {
                player.displayClientMessage(Component.translatable("message.ob.no_dimension"), true);
            }
            return;
        }

        //Each placed swapper remembers its own target on its block entity
        swapper.link(target.dimension());

        //Put a partner swapper in the target dimension that points back here
        if (!(target.getBlockState(pos).getBlock() instanceof SeedSwapper))
        {
            target.setBlock(pos, state, Block.UPDATE_ALL);
        }
        if (target.getBlockEntity(pos) instanceof SeedSwapperBlockEntity partner)
        {
            partner.link(level.dimension());
            logger.info("Linked seed swapper at {} between {} and {}", pos, level.dimension().location(), target.dimension().location());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof SeedSwapperBlockEntity swapper)
        {
            swapper.activate(serverLevel, pos, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Picks a random dimension other than the current one where the swapper's position is inside the build height,
     * or null if there isn't one.
     */
    @Nullable
    private static ServerLevel pickTargetDim(ServerLevel level, BlockPos pos)
    {
        List<ServerLevel> dimensions = new ArrayList<>();
        for (ResourceKey<Level> key : level.getServer().levelKeys())
        {
            ServerLevel other = level.getServer().getLevel(key);
            if (other != null && other != level && !other.isOutsideBuildHeight(pos))
            {
                dimensions.add(other);
            }
        }
        if (dimensions.isEmpty())
        {
            return null;
        }
        return dimensions.get(level.random.nextInt(dimensions.size()));
    }
}
