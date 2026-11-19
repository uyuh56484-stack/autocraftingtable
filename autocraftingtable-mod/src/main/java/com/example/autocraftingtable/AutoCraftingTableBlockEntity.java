package com.example.autocraftingtable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCraftingTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_RECIPES = 6;
    public static final int TICK_INTERVAL = 100;

    private final List<SavedRecipe> recipes = new ArrayList<>();
    /** Synced to the client: slot i holds the result of recipe i. */
    public final SimpleContainer resultDisplay = new SimpleContainer(MAX_RECIPES);
    private int cooldown = 0;

    public AutoCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.AUTO_CRAFTING_TABLE_BE.get(), pos, state);
    }

    public boolean addRecipe(List<ItemStack> grid, Level level) {
        if (recipes.size() >= MAX_RECIPES) return false;
        TransientCraftingContainer container = new TransientCraftingContainer(new DummyMenu(), 3, 3);
        for (int i = 0; i < 9; i++) container.setItem(i, grid.get(i));
        var holder = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, container.asCraftInput(), level)
                .orElse(null);
        if (holder == null) return false;
        ItemStack result = holder.value().assemble(container.asCraftInput(), level.registryAccess());
        if (result.isEmpty()) return false;
        recipes.add(new SavedRecipe(List.copyOf(grid), result));
        resultDisplay.setItem(recipes.size() - 1, result);
        setChanged();
        return true;
    }

    public void removeRecipe(int index) {
        if (index < 0 || index >= recipes.size()) return;
        recipes.remove(index);
        refreshDisplay();
        setChanged();
    }

    private void refreshDisplay() {
        for (int i = 0; i < MAX_RECIPES; i++) {
            resultDisplay.setItem(i, i < recipes.size() ? recipes.get(i).result() : ItemStack.EMPTY);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutoCraftingTableBlockEntity be) {
        if (++be.cooldown < TICK_INTERVAL) return;
        be.cooldown = 0;
        for (int i = 0; i < be.recipes.size(); i++) {
            be.tryCraft(i);
        }
    }

    public void tryCraft(int index) {
        if (getLevel() == null || getLevel().isClientSide) return;
        if (index < 0 || index >= recipes.size()) return;
        Level level = getLevel();
        SavedRecipe recipe = recipes.get(index);

        List<Container> chests = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            var neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor instanceof Container c && !(neighbor instanceof AutoCraftingTableBlockEntity)) {
                chests.add(c);
            }
        }
        if (chests.isEmpty()) return;

        // protection against infinite loops: skip if the result is already present in a chest
        for (Container c : chests) {
            for (int s = 0; s < c.getContainerSize(); s++) {
                if (ItemStack.isSameItemSameComponents(c.getItem(s), recipe.result())) return;
            }
        }

        Map<net.minecraft.world.item.Item, Integer> needed = new HashMap<>();
        for (ItemStack ing : recipe.ingredients()) {
            if (!ing.isEmpty()) needed.merge(ing.getItem(), ing.getCount(), Integer::sum);
        }
        if (needed.isEmpty()) return;

        // check availability
        for (Map.Entry<net.minecraft.world.item.Item, Integer> e : needed.entrySet()) {
            int have = 0;
            for (Container c : chests) {
                for (int s = 0; s < c.getContainerSize(); s++) {
                    if (c.getItem(s).is(e.getKey())) have += c.getItem(s).getCount();
                }
            }
            if (have < e.getValue()) return;
        }

        // consume ingredients
        for (Map.Entry<net.minecraft.world.item.Item, Integer> e : needed.entrySet()) {
            int remaining = e.getValue();
            for (Container c : chests) {
                for (int s = 0; s < c.getContainerSize() && remaining > 0; s++) {
                    ItemStack st = c.getItem(s);
                    if (st.is(e.getKey())) {
                        int take = Math.min(remaining, st.getCount());
                        st.shrink(take);
                        if (st.isEmpty()) c.setItem(s, ItemStack.EMPTY);
                        remaining -= take;
                    }
                }
            }
        }
        for (Container c : chests) c.setChanged();

        // insert the result
        ItemStack result = recipe.result().copy();
        for (Container c : chests) {
            for (int s = 0; s < c.getContainerSize() && !result.isEmpty(); s++) {
                ItemStack st = c.getItem(s);
                if (st.isEmpty()) {
                    c.setItem(s, result.copy());
                    result.setCount(0);
                } else if (ItemStack.isSameItemSameComponents(st, result)
                        && st.getCount() + result.getCount() <= st.getMaxStackSize()) {
                    st.grow(result.getCount());
                    result.setCount(0);
                }
            }
            if (result.isEmpty()) break;
        }
        if (!result.isEmpty()) {
            ItemEntity drop = new ItemEntity(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, result);
            level.addFreshEntity(drop);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (SavedRecipe r : recipes) list.add(r.save(registries));
        tag.put("Recipes", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        recipes.clear();
        ListTag list = tag.getList("Recipes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            SavedRecipe r = SavedRecipe.load(list.getCompound(i), registries);
            if (r != null) recipes.add(r);
        }
        refreshDisplay();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block." + AutoCraftingTableMod.MODID + ".auto_crafting_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        AutoCraftMenu menu = new AutoCraftMenu(id, inv, this.resultDisplay);
        menu.setServerPos(this.worldPosition);
        return menu;
    }

    private static class DummyMenu extends AbstractContainerMenu {
        DummyMenu() {
            super(MenuType.GENERIC_3x3, 0);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
