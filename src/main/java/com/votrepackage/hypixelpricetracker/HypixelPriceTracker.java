package com.votrepackage.hypixelpricetracker;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypixelPriceTracker implements ModInitializer {
    public static final String MOD_ID = "hypixel-price-tracker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initialisation de Hypixel Price Tracker");
        
        // Initialisation des services
        PriceApiService.initialize();
        
        LOGGER.info("Hypixel Price Tracker initialisé avec succès!");
    }
}