package com.ironmanqol;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.InterfaceID;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class ShopManager 
{
    @Inject
    private Client client;

    // Track original shop inventories (shop ID -> set of original item IDs)
    private final Map<Integer, Set<Integer>> originalShopStock = new ConcurrentHashMap<>();
    
    // Track shop inventories over time to detect player-sold items
    private final Map<Integer, Map<Integer, ShopItemInfo>> currentShopStock = new ConcurrentHashMap<>();
    
    // Known shops and their standard inventories
    private final Map<String, Set<Integer>> knownShopStock = new ConcurrentHashMap<>();

    private static class ShopItemInfo 
    {
        final int itemId;
        final int quantity;
        final boolean isOriginal;
        final long firstSeen;

        ShopItemInfo(int itemId, int quantity, boolean isOriginal) 
        {
            this.itemId = itemId;
            this.quantity = quantity;
            this.isOriginal = isOriginal;
            this.firstSeen = System.currentTimeMillis();
        }
    }

    // Alternative approach if ShopOpened doesn't exist
    public void onWidgetLoaded(WidgetLoaded event) 
    {
        // Check if shop interface was loaded
        if (event.getGroupId() == InterfaceID.SHOP) 
        {
            // Initialize shop tracking when shop interface opens
            initializeShopFromWidget();
        }
    }

    private void initializeShopFromWidget() 
    {
        Widget shopWidget = client.getWidget(InterfaceID.SHOP, 3); // Shop items container
        if (shopWidget == null) 
        {
            return;
        }

        // Generate a shop ID based on widget contents or location
        int shopId = generateShopId();
        String shopName = getShopNameFromWidget();
        
        System.out.println("Shop detected: " + shopName + " (ID: " + shopId + ")");
        
        // Initialize original stock tracking if this is the first time seeing this shop
        if (!originalShopStock.containsKey(shopId)) 
        {
            Set<Integer> originalItems = new HashSet<>();
            Map<Integer, ShopItemInfo> currentItems = new HashMap<>();
            
            Widget[] shopItems = shopWidget.getChildren();
            if (shopItems != null) 
            {
                for (Widget itemWidget : shopItems) 
                {
                    if (itemWidget != null && itemWidget.getItemId() != -1) 
                    {
                        int itemId = itemWidget.getItemId();
                        boolean isOriginal = isKnownShopItem(shopName, itemId);
                        
                        originalItems.add(itemId);
                        currentItems.put(itemId, new ShopItemInfo(itemId, itemWidget.getItemQuantity(), isOriginal));
                    }
                }
            }
            
            originalShopStock.put(shopId, originalItems);
            currentShopStock.put(shopId, currentItems);
        }
        else 
        {
            // Update current stock
            updateShopStockFromWidget(shopId);
        }
    }

    private int generateShopId() 
    {
        // Generate shop ID based on player location or shop interface content
        Player player = client.getLocalPlayer();
        if (player != null) 
        {
            WorldPoint location = player.getWorldLocation();
            return location.hashCode(); // Simple hash based on location
        }
        return -1;
    }

    private String getShopNameFromWidget() 
    {
        // Try to get shop name from the shop interface
        Widget shopNameWidget = client.getWidget(InterfaceID.SHOP, 1);
        if (shopNameWidget != null && shopNameWidget.getText() != null) 
        {
            return shopNameWidget.getText();
        }
        
        // Fallback to location-based name
        Player player = client.getLocalPlayer();
        if (player != null) 
        {
            WorldPoint location = player.getWorldLocation();
            return "Shop_" + location.getX() + "_" + location.getY();
        }
        
        return "Unknown_Shop";
    }

    public void updateShopStockFromWidget(int shopId) 
    {
        Map<Integer, ShopItemInfo> current = currentShopStock.get(shopId);
        if (current == null) 
        {
            return;
        }

        Set<Integer> currentItemIds = new HashSet<>();
        Widget shopWidget = client.getWidget(InterfaceID.SHOP, 3);
        
        if (shopWidget != null && shopWidget.getChildren() != null) 
        {
            for (Widget itemWidget : shopWidget.getChildren()) 
            {
                if (itemWidget != null && itemWidget.getItemId() != -1) 
                {
                    int itemId = itemWidget.getItemId();
                    currentItemIds.add(itemId);
                    
                    if (!current.containsKey(itemId)) 
                    {
                        // New item appeared - likely sold by a player
                        String shopName = getShopNameFromWidget();
                        boolean isOriginal = isKnownShopItem(shopName, itemId);
                        current.put(itemId, new ShopItemInfo(itemId, itemWidget.getItemQuantity(), isOriginal));
                    }
                }
            }
        }
        
        // Remove items that are no longer in the shop
        current.keySet().retainAll(currentItemIds);
    }

    public boolean canBuyItem(int shopId, int itemId) 
    {
        // Check if this item is part of the original shop stock
        Set<Integer> originalItems = originalShopStock.get(shopId);
        if (originalItems != null && originalItems.contains(itemId)) 
        {
            return true;
        }
        
        // Check if this is a known shop item based on shop analysis
        Map<Integer, ShopItemInfo> currentItems = currentShopStock.get(shopId);
        if (currentItems != null) 
        {
            ShopItemInfo itemInfo = currentItems.get(itemId);
            return itemInfo != null && itemInfo.isOriginal;
        }
        
        return false;
    }

    public boolean isPlayerSoldItem(int shopId, int itemId) 
    {
        return !canBuyItem(shopId, itemId);
    }

    private boolean isKnownShopItem(String shopName, int itemId) 
    {
        // Check against known shop inventories
        Set<Integer> knownItems = knownShopStock.get(shopName);
        if (knownItems != null) 
        {
            return knownItems.contains(itemId);
        }
        
        // Default logic for common shop types
        return isLikelyOriginalShopItem(shopName, itemId);
    }

    private boolean isLikelyOriginalShopItem(String shopName, int itemId) 
    {
        // Basic heuristics for determining if an item is likely original shop stock
        // This could be expanded with more sophisticated logic
        
        // Common patterns for shop items vs player-sold items
        ItemComposition itemComp = client.getItemDefinition(itemId);
        if (itemComp == null) 
        {
            return false;
        }
        
        String itemName = itemComp.getName().toLowerCase();
        
        // Items that are commonly sold by players (usually valuable or processed items)
        if (isHighValueItem(itemId) || isProcessedItem(itemName)) 
        {
            return false;
        }
        
        // Basic supplies are usually shop stock
        if (isBasicSupply(itemName)) 
        {
            return true;
        }
        
        return true; // Default to allowing when uncertain
    }

    private boolean isHighValueItem(int itemId) 
    {
        // Check if item is high-value (commonly sold by players)
        ItemComposition itemComp = client.getItemDefinition(itemId);
        if (itemComp == null) 
        {
            return false;
        }
        
        // High-alch value heuristic
        return itemComp.getPrice() > 1000;
    }

    private boolean isProcessedItem(String itemName) 
    {
        // Check for items that are typically player-made/processed
        String[] processedKeywords = {
            "cooked", "roasted", "baked", "barbecued",
            "potion", "brew", "mix", "dose",
            "enchanted", "magic", "rune",
            "crafted", "smithed", "carved"
        };
        
        for (String keyword : processedKeywords) 
        {
            if (itemName.contains(keyword)) 
            {
                return true;
            }
        }
        
        return false;
    }

    private boolean isBasicSupply(String itemName) 
    {
        // Check for basic shop supplies
        String[] basicSupplyKeywords = {
            "arrow", "bolt", "needle", "thread",
            "chisel", "hammer", "tinderbox",
            "bucket", "jug", "vial", "bowl",
            "knife", "rope", "candle"
        };
        
        for (String keyword : basicSupplyKeywords) 
        {
            if (itemName.contains(keyword)) 
            {
                return true;
            }
        }
        
        return false;
    }

    public void loadKnownShopData() 
    {
        // Load known shop inventories
        // This would ideally be loaded from a data file
        
        // Example shop data
        Set<Integer> generalStore = new HashSet<>();
        generalStore.add(ItemID.BUCKET);
        generalStore.add(ItemID.TINDERBOX);
        generalStore.add(ItemID.CHISEL);
        generalStore.add(ItemID.HAMMER);
        knownShopStock.put("General Store", generalStore);
        
        Set<Integer> rangeShop = new HashSet<>();
        rangeShop.add(ItemID.BRONZE_ARROW);
        rangeShop.add(ItemID.IRON_ARROW);
        rangeShop.add(ItemID.BRONZE_BOLTS);
        rangeShop.add(ItemID.SHORTBOW);
        knownShopStock.put("Ranging Shop", rangeShop);
        
        // Add more shop data as needed
    }

    public Set<Integer> getOriginalStock(int shopId) 
    {
        return originalShopStock.getOrDefault(shopId, Collections.emptySet());
    }

    public void reset() 
    {
        originalShopStock.clear();
        currentShopStock.clear();
        // Don't clear knownShopStock as it's static data
    }

    public void cleanup() 
    {
        // Clean up old shop data if needed
        // For now, we keep all data as shops don't change frequently
    }

    public int getCurrentShopId() 
    {
        // Get the currently open shop ID
        Widget shopWidget = client.getWidget(InterfaceID.SHOP, 2);
        if (shopWidget != null) 
        {
            // The shop ID might be stored in widget configuration
            // This is a simplified approach - actual implementation would need
            // to properly extract the shop ID from the interface
            return shopWidget.getId();
        }
        return -1;
    }

    public List<Integer> getFilteredShopItems(int shopId) 
    {
        List<Integer> filteredItems = new ArrayList<>();
        Set<Integer> originalItems = originalShopStock.get(shopId);
        
        if (originalItems != null) 
        {
            filteredItems.addAll(originalItems);
        }
        
        return filteredItems;
    }

    public boolean shouldHideShopItem(int shopId, int itemId) 
    {
        return !canBuyItem(shopId, itemId);
    }
}