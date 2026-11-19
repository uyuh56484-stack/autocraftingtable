package com.example.autocraftingtable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(AutoCraftingTableMod.MODID)
public class AutoCraftingTableMod {
    public static final String MODID = "autocraftingtable";

    public AutoCraftingTableMod(IEventBus modEventBus, ModContainer modContainer) {
        ModRegistry.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::registerScreens);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        ModNetwork.register(registrar);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistry.AUTO_CRAFT_MENU.get(), AutoCraftScreen::new);
    }
}
