package com.example.autocraftingtable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

public class ModNetwork {

    public record AddRecipePayload(BlockPos pos, List<ItemStack> items) implements CustomPacketPayload {
        public static final Type<AddRecipePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AutoCraftingTableMod.MODID, "add_recipe"));

        public static final StreamCodec<RegistryFriendlyByteBuf, AddRecipePayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public AddRecipePayload decode(RegistryFriendlyByteBuf buf) {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < 9; i++) {
                    items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                }
                return new AddRecipePayload(pos, items);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AddRecipePayload p) {
                BlockPos.STREAM_CODEC.encode(buf, p.pos);
                for (int i = 0; i < 9; i++) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, p.items.get(i));
                }
            }
        };

        @Override
        public Type<AddRecipePayload> type() {
            return TYPE;
        }
    }

    public record RemoveRecipePayload(BlockPos pos, int index) implements CustomPacketPayload {
        public static final Type<RemoveRecipePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AutoCraftingTableMod.MODID, "remove_recipe"));

        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveRecipePayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public RemoveRecipePayload decode(RegistryFriendlyByteBuf buf) {
                return new RemoveRecipePayload(BlockPos.STREAM_CODEC.decode(buf), buf.readVarInt());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, RemoveRecipePayload p) {
                BlockPos.STREAM_CODEC.encode(buf, p.pos);
                buf.writeVarInt(p.index);
            }
        };

        @Override
        public Type<RemoveRecipePayload> type() {
            return TYPE;
        }
    }

    public record CraftNowPayload(BlockPos pos, int index) implements CustomPacketPayload {
        public static final Type<CraftNowPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AutoCraftingTableMod.MODID, "craft_now"));

        public static final StreamCodec<RegistryFriendlyByteBuf, CraftNowPayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public CraftNowPayload decode(RegistryFriendlyByteBuf buf) {
                return new CraftNowPayload(BlockPos.STREAM_CODEC.decode(buf), buf.readVarInt());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, CraftNowPayload p) {
                BlockPos.STREAM_CODEC.encode(buf, p.pos);
                buf.writeVarInt(p.index);
            }
        };

        @Override
        public Type<CraftNowPayload> type() {
            return TYPE;
        }
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(AddRecipePayload.TYPE, AddRecipePayload.STREAM_CODEC, ModNetwork::handleAdd);
        registrar.playToServer(RemoveRecipePayload.TYPE, RemoveRecipePayload.STREAM_CODEC, ModNetwork::handleRemove);
        registrar.playToServer(CraftNowPayload.TYPE, CraftNowPayload.STREAM_CODEC, ModNetwork::handleCraftNow);
    }

    private static void handleAdd(AddRecipePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.getBlockEntity(payload.pos()) instanceof AutoCraftingTableBlockEntity be) {
                be.addRecipe(payload.items(), level);
            }
        });
    }

    private static void handleRemove(RemoveRecipePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.getBlockEntity(payload.pos()) instanceof AutoCraftingTableBlockEntity be) {
                be.removeRecipe(payload.index());
            }
        });
    }

    private static void handleCraftNow(CraftNowPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.getBlockEntity(payload.pos()) instanceof AutoCraftingTableBlockEntity be) {
                be.tryCraft(payload.index());
            }
        });
    }
}
