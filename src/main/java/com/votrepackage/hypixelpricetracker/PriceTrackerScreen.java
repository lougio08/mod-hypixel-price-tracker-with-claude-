package com.votrepackage.hypixelpricetracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceTrackerScreen extends Screen {
    private TextFieldWidget searchField;
    private List<PriceApiService.ItemPriceData> displayedItems;
    private int scrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 10;

    public PriceTrackerScreen() {
        super(Text.literal("Hypixel Price Tracker"));
        this.displayedItems = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        // Champ de recherche
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 30, 200, 20, Text.literal("Rechercher..."));
        this.searchField.setChangedListener(this::onSearchChanged);
        this.addSelectableChild(this.searchField);

        // Bouton de rafraîchissement
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Rafraîchir"), button -> {
            PriceApiService.updatePrices();
            updateDisplayedItems();
        }).dimensions(this.width / 2 + 110, 30, 80, 20).build());

        // Bouton fermer
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Fermer"), button -> {
            this.close();
        }).dimensions(this.width / 2 - 40, this.height - 30, 80, 20).build());

        updateDisplayedItems();
    }

    private void onSearchChanged(String search) {
        scrollOffset = 0;
        updateDisplayedItems();
    }

    private void updateDisplayedItems() {
        displayedItems.clear();
        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        
        Map<String, PriceApiService.ItemPriceData> allPrices = PriceApiService.getAllPrices();
        
        for (PriceApiService.ItemPriceData item : allPrices.values()) {
            if (search.isEmpty() || item.getItemId().toLowerCase().contains(search)) {
                displayedItems.add(item);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Titre
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        // Champ de recherche
        if (searchField != null) {
            searchField.render(context, mouseX, mouseY, delta);
        }

        // En-têtes des colonnes
        int startY = 60;
        context.drawTextWithShadow(this.textRenderer, "Item", 20, startY, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Prix d'achat", this.width / 2 - 80, startY, 0x55FF55);
        context.drawTextWithShadow(this.textRenderer, "Prix de vente", this.width / 2 + 20, startY, 0xFF5555);

        // Liste des items
        int y = startY + 20;
        int endIndex = Math.min(scrollOffset + ITEMS_PER_PAGE, displayedItems.size());
        
        for (int i = scrollOffset; i < endIndex; i++) {
            PriceApiService.ItemPriceData item = displayedItems.get(i);
            
            // Nom de l'item
            String itemName = formatItemName(item.getItemId());
            context.drawTextWithShadow(this.textRenderer, itemName, 20, y, 0xFFFFFF);
            
            // Prix d'achat
            context.drawTextWithShadow(this.textRenderer, item.getFormattedBuyPrice(), this.width / 2 - 80, y, 0x55FF55);
            
            // Prix de vente
            context.drawTextWithShadow(this.textRenderer, item.getFormattedSellPrice(), this.width / 2 + 20, y, 0xFF5555);
            
            y += 15;
        }

        // Informations de défilement
        if (displayedItems.size() > ITEMS_PER_PAGE) {
            String scrollInfo = String.format("Page %d/%d", 
                (scrollOffset / ITEMS_PER_PAGE) + 1, 
                (displayedItems.size() - 1) / ITEMS_PER_PAGE + 1);
            context.drawTextWithShadow(this.textRenderer, scrollInfo, 20, this.height - 50, 0xAAAAAA);
        }

        // Status de la connexion
        String status = PriceApiService.isCacheValid() ? 
            Text.literal("Connecté").formatted(Formatting.GREEN).getString() : 
            Text.literal("Déconnecté").formatted(Formatting.RED).getString();
        context.drawTextWithShadow(this.textRenderer, "Status: " + status, this.width - 120, this.height - 30, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
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
    public boolean shouldPause() {
        return false;
    }
}