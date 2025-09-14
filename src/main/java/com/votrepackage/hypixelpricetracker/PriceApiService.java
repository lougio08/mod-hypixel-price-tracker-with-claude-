package com.votrepackage.hypixelpricetracker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PriceApiService {
    private static final String API_URL = "https://api.hypixel.net/skyblock/bazaar";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private static Map<String, ItemPriceData> priceCache = new HashMap<>();
    private static long lastUpdate = 0;
    private static final long CACHE_DURATION = 30000; // 30 secondes

    public static void initialize() {
        // Mise à jour automatique toutes les minutes
        scheduler.scheduleAtFixedRate(PriceApiService::updatePrices, 0, 1, TimeUnit.MINUTES);
    }

    public static void updatePrices() {
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("User-Agent", "Minecraft-Hypixel-Price-Tracker/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                    JsonObject products = jsonResponse.getAsJsonObject("products");
                    
                    Map<String, ItemPriceData> newPrices = new HashMap<>();
                    
                    products.entrySet().forEach(entry -> {
                        String itemId = entry.getKey();
                        JsonObject itemData = entry.getValue().getAsJsonObject();
                        
                        JsonObject quickStatus = itemData.getAsJsonObject("quick_status");
                        if (quickStatus != null) {
                            double buyPrice = quickStatus.get("buyPrice").getAsDouble();
                            double sellPrice = quickStatus.get("sellPrice").getAsDouble();
                            
                            newPrices.put(itemId, new ItemPriceData(itemId, buyPrice, sellPrice));
                        }
                    });
                    
                    priceCache = newPrices;
                    lastUpdate = System.currentTimeMillis();
                    
                    HypixelPriceTracker.LOGGER.info("Prix mis à jour: {} items", newPrices.size());
                }
                
                return null;
            } catch (Exception e) {
                HypixelPriceTracker.LOGGER.error("Erreur lors de la récupération des prix", e);
                return null;
            }
        });
    }

    public static ItemPriceData getPrice(String itemId) {
        return priceCache.get(itemId);
    }

    public static Map<String, ItemPriceData> getAllPrices() {
        return new HashMap<>(priceCache);
    }

    public static boolean isCacheValid() {
        return System.currentTimeMillis() - lastUpdate < CACHE_DURATION;
    }

    public static class ItemPriceData {
        private final String itemId;
        private final double buyPrice;
        private final double sellPrice;

        public ItemPriceData(String itemId, double buyPrice, double sellPrice) {
            this.itemId = itemId;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }

        public String getItemId() { return itemId; }
        public double getBuyPrice() { return buyPrice; }
        public double getSellPrice() { return sellPrice; }
        
        public String getFormattedBuyPrice() {
            return formatPrice(buyPrice);
        }
        
        public String getFormattedSellPrice() {
            return formatPrice(sellPrice);
        }
        
        private String formatPrice(double price) {
            if (price >= 1000000) {
                return String.format("%.1fM", price / 1000000);
            } else if (price >= 1000) {
                return String.format("%.1fK", price / 1000);
            } else {
                return String.format("%.1f", price);
            }
        }
    }
}