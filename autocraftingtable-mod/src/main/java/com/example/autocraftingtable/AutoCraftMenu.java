package com.example.autocraftingtable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AutoCraftMenu extends AbstractContainerMenu {
    /** slots 0-8: craft grid, 9-14: recipe display (ghost), 15-41: player inv, 42-50: hotbar */
    public final TransientCraftingContainer craftGrid;
    public final Container results;
    /** Syncs the block position of the table to the client. */
    private final ContainerData posData = new SimpleContainerData(3);

    public AutoCraftMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(AutoCraftingTableBlockEntity.MAX_RECIPES));
    }

    public AutoCraftMenu(int id, Inventory playerInv, Container results) {
        super(ModRegistry.AUTO_CRAFT_MENU.get(), id);
        this.craftGrid = new TransientCraftingContainer(this, 3, 3);
        this.results = results;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                this.addSlot(new Slot(this.craftGrid, r * 3 + c, 8 + c * 18, 18 + r * 18));
            }
        }
        for (int i = 0; i < AutoCraftingTableBlockEntity.MAX_RECIPES; i++) {
            this.addSlot(new DisplaySlot(this.results, i, 8, 84 + i * 16));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 178 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 236));
        }
        this.addDataSlots(this.posData);
    }

    public void setServerPos(BlockPos pos) {
        this.posData.set(0, pos.getX());
        this.posData.set(1, pos.getY());
        this.posData.set(2, pos.getZ());
    }

    public BlockPos getTargetPos() {
        return new BlockPos(this.posData.get(0), this.posData.get(1), this.posData.get(2));
    }

    public List<ItemStack> getGridItems() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < 9; i++) out.add(this.craftGrid.getItem(i));
        return out;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 9) {
            if (!this.moveItemStackTo(stack, 15, 51, true)) return ItemStack.EMPTY;
        } else if (index >= 15) {
            if (!this.moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static class DisplaySlot extends Slot {
        public DisplaySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
