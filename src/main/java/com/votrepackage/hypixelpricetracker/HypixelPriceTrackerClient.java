package com.votrepackage.hypixelpricetracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class HypixelPriceTrackerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HypixelPriceTracker.LOGGER.info("Hypixel Price Tracker Client - Chargé avec succès!");
        
        // Enregistre une commande simple pour tester
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("hpt")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("Hypixel Price Tracker fonctionne !"));
                    return 1;
                }));
        });
    }
}