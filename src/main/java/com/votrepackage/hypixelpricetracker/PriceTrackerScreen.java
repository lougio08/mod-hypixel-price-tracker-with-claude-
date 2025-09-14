package com.votrepackage.hypixelpricetracker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceTrackerScreen extends Screen {
    private EditBox searchField;
    private List<PriceApiService.ItemPriceData> displayedItems;
    private int scrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 10;

    public PriceTrackerScreen() {
        super(Component.literal("Hypixel Price Tracker"));
        this.displayedItems = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        // Champ de recherche
        this.searchField = new EditBox(this.font, this.width / 2 - 100, 30, 200, 20, Component.literal("Rechercher..."));
        this.searchField.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchField);

        // Bouton de rafraîchissement
        this.addRenderableWidget(Button.builder(Component.literal("Rafraîchir"), button -> {
            PriceApiService.updatePrices();
            updateDisplayedItems();
        }).bounds(this.width / 2 + 110, 30, 80, 20).build());

        // Bouton fermer
        this.addRenderableWidget(Button.builder(Component.literal("Fermer"), button -> {
            this.onClose();
        }).bounds(this.width / 2 - 40, this.height - 30, 80, 20).build());

        updateDisplayedItems();
    }

    private void onSearchChanged(String search) {
        scrollOffset = 0;
        updateDisplayedItems();
    }

    private void updateDisplayedItems() {
        displayedItems.clear();
        String search = searchField != null ? searchField.getValue().toLowerCase() : "";
        
        Map<String, PriceApiService.ItemPriceData> allPrices = PriceApiService.getAllPrices();
        
        for (PriceApiService.ItemPriceData item : allPrices.values()) {
            if (search.isEmpty() || item.getItemId().toLowerCase().contains(search)) {
                displayedItems.add(item);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);

        // Titre
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Champ de recherche
        if (searchField != null) {
            searchField.render(guiGraphics, mouseX, mouseY, delta);
        }

        // En-têtes des colonnes
        int startY = 60;
        guiGraphics.drawString(this.font, "Item", 20, startY, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Prix d'achat", this.width / 2 - 80, startY, 0x55FF55);
        guiGraphics.drawString(this.font, "Prix de vente", this.width / 2 + 20, startY, 0xFF5555);

        // Liste des items
        int y = startY + 20;
        int endIndex = Math.min(scrollOffset + ITEMS_PER_PAGE, displayedItems.size());
        
        for (int i = scrollOffset; i < endIndex; i++) {
            PriceApiService.ItemPriceData item = displayedItems.get(i);
            
            // Nom de l'item
            String itemName = formatItemName(item.getItemId());
            guiGraphics.drawString(this.font, itemName, 20, y, 0xFFFFFF);
            
            // Prix d'achat
            guiGraphics.drawString(this.font, item.getFormattedBuyPrice(), this.width / 2 - 80, y, 0x55FF55);
            
            // Prix de vente
            guiGraphics.drawString(this.font, item.getFormattedSellPrice(), this.width / 2 + 20, y, 0xFF5555);
            
            y += 15;
        }

        // Informations de défilement
        if (displayedItems.size() > ITEMS_PER_PAGE) {
            String scrollInfo = String.format("Page %d/%d", 
                (scrollOffset / ITEMS_PER_PAGE) + 1, 
                (displayedItems.size() - 1) / ITEMS_PER_PAGE + 1);
            guiGraphics.drawString(this.font, scrollInfo, 20, this.height - 50, 0xAAAAAA);
        }

        // Status de la connexion
        String status = PriceApiService.isCacheValid() ? 
            Component.literal("Connecté").withStyle(ChatFormatting.GREEN).getString() : 
            Component.literal("Déconnecté").withStyle(ChatFormatting.RED).getString();
        guiGraphics.drawString(this.font, "Status: " + status, this.width - 120, this.height - 30, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    private String formatItemName(String itemId) {
        return itemId.toLowerCase().replace("_", " ");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (displayedItems.size() > ITEMS_PER_PAGE) {
            if (verticalAmount > 0 && scrollOffset > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (verticalAmount < 0 && scrollOffset + ITEMS_PER_PAGE < displayedItems.size()) {
                scrollOffset = Math.min(displayedItems.size() - ITEMS_PER_PAGE, scrollOffset + 1);
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}