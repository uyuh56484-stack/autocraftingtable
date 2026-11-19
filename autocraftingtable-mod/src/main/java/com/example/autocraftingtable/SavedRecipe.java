package com.example.autocraftingtable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SavedRecipe(List<ItemStack> ingredients, ItemStack result) {

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (ItemStack st : ingredients) {
            items.add(st.save(registries));
        }
        tag.put("Ingredients", items);
        tag.put("Result", result.save(registries));
        return tag;
    }

    public static SavedRecipe load(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag items = tag.getList("Ingredients", Tag.TAG_COMPOUND);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            stacks.add(ItemStack.parse(registries, items.getCompound(i)).orElse(ItemStack.EMPTY));
        }
        ItemStack result = ItemStack.parse(registries, tag.getCompound("Result")).orElse(ItemStack.EMPTY);
        if (result.isEmpty() || stacks.size() != 9) return null;
        return new SavedRecipe(stacks, result);
    }
}
