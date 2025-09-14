package com.votrepackage.hypixelpricetracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HypixelPriceTrackerClient implements ClientModInitializer {
    private static KeyBinding openPriceGui;

    @Override
    public void onInitializeClient() {
        HypixelPriceTracker.LOGGER.info("Initialisation du client Hypixel Price Tracker");

        // Enregistrement des keybindings
        openPriceGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hypixel-price-tracker.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.hypixel-price-tracker"
        ));

        // Gestion des événements de touches
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openPriceGui.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new PriceTrackerScreen());
                }
            }
        });

        HypixelPriceTracker.LOGGER.info("Client Hypixel Price Tracker initialisé avec succès!");
    }
}