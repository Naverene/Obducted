package net.neverandy.ob;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neverandy.ob.blocks.SeedSwapper;
import net.neverandy.ob.blocks.SeedSwapperBlockEntity;
import org.slf4j.Logger;

/**
 * Created by awweaver on 4/26/17.
 */
@Mod(Obducted.MOD_ID)
public class Obducted
{
    public static final String MOD_ID = "ob";
    public static final Logger logger = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<SeedSwapper> SEED_SWAPPER = BLOCKS.register("seedswapper",
            () -> new SeedSwapper(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANVIL)));

    public static final DeferredItem<BlockItem> SEED_SWAPPER_ITEM = ITEMS.registerSimpleBlockItem(SEED_SWAPPER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeedSwapperBlockEntity>> SEED_SWAPPER_BLOCK_ENTITY = BLOCK_ENTITIES.register("seedswapper",
            () -> BlockEntityType.Builder.of(SeedSwapperBlockEntity::new, SEED_SWAPPER.get()).build(null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("items",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ob"))
                    .icon(() -> SEED_SWAPPER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(SEED_SWAPPER_ITEM.get()))
                    .build());

    public Obducted(IEventBus modEventBus)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        TABS.register(modEventBus);
    }
}
