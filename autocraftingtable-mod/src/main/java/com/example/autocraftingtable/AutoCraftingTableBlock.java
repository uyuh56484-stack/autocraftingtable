package com.example.autocraftingtable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AutoCraftingTableBlock extends CraftingTableBlock {

    public AutoCraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AutoCraftingTableBlockEntity) {
                player.openMenu((AutoCraftingTableBlockEntity) be, pos);
                return InteractionResult.CONSUME;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModRegistry.AUTO_CRAFTING_TABLE_BE.get()) {
            return null;
        }
        BlockEntityTicker<AutoCraftingTableBlockEntity> ticker = AutoCraftingTableBlockEntity::serverTick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }
}
