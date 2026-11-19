package com.example.autocraftingtable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AutoCraftScreen extends AbstractContainerScreen<AutoCraftMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AutoCraftingTableMod.MODID, "textures/gui/auto_craft_screen.png");

    public AutoCraftScreen(AutoCraftMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 260;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 222;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.autocraftingtable.add"),
                        b -> PacketDistributor.sendToServer(new ModNetwork.AddRecipePayload(
                                this.menu.getTargetPos(), this.menu.getGridItems())))
                .bounds(this.leftPos + 104, this.topPos + 26, 64, 20)
                .build());

        for (int i = 0; i < AutoCraftingTableBlockEntity.MAX_RECIPES; i++) {
            int idx = i;
            this.addRenderableWidget(Button.builder(
                            Component.literal("X"),
                            b -> PacketDistributor.sendToServer(new ModNetwork.RemoveRecipePayload(
                                    this.menu.getTargetPos(), idx)))
                    .bounds(this.leftPos + 128, this.topPos + 84 + i * 16, 14, 14)
                    .build());
            this.addRenderableWidget(Button.builder(
                            Component.literal(">"),
                            b -> PacketDistributor.sendToServer(new ModNetwork.CraftNowPayload(
                                    this.menu.getTargetPos(), idx)))
                    .bounds(this.leftPos + 150, this.topPos + 84 + i * 16, 14, 14)
                    .build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font,
                Component.translatable("gui.autocraftingtable.list_title"), 8, 72, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 512);
    }
}
