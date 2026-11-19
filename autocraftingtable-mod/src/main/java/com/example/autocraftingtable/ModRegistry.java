package com.example.autocraftingtable;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AutoCraftingTableMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AutoCraftingTableMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AutoCraftingTableMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AutoCraftingTableMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AutoCraftingTableMod.MODID);

    public static final DeferredBlock<AutoCraftingTableBlock> AUTO_CRAFTING_TABLE = BLOCKS.register("auto_crafting_table",
            () -> new AutoCraftingTableBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> AUTO_CRAFTING_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem("auto_crafting_table", AUTO_CRAFTING_TABLE);

    public static final Supplier<BlockEntityType<AutoCraftingTableBlockEntity>> AUTO_CRAFTING_TABLE_BE =
            BLOCK_ENTITIES.register("auto_crafting_table",
                    () -> BlockEntityType.Builder.of(AutoCraftingTableBlockEntity::new, AUTO_CRAFTING_TABLE.get()).build(null));

    public static final Supplier<MenuType<AutoCraftMenu>> AUTO_CRAFT_MENU =
            MENUS.register("auto_craft_menu", () -> new MenuType<>(AutoCraftMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AUTO_CRAFT_TAB = TABS.register("auto_crafting_table",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + AutoCraftingTableMod.MODID))
                    .icon(() -> new ItemStack(AUTO_CRAFTING_TABLE_ITEM.get()))
                    .displayItems((params, output) -> output.accept(AUTO_CRAFTING_TABLE_ITEM.get()))
                    .build());

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        TABS.register(bus);
    }
}
