package com.ironmanqol;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemUtils 
{
    // Common shop items that are typically original stock
    public static final Set<Integer> COMMON_SHOP_ITEMS = new HashSet<>();
    
    // Items commonly sold by players to shops
    public static final Set<Integer> COMMONLY_PLAYER_SOLD_ITEMS = new HashSet<>();
    
    // Known static spawn items
    public static final Set<Integer> STATIC_SPAWN_ITEMS = new HashSet<>();
    
    static 
    {
        initializeCommonShopItems();
        initializePlayerSoldItems();
        initializeStaticSpawnItems();
    }
    
    private static void initializeCommonShopItems() 
    {
        // Basic tools and supplies commonly sold in shops
        COMMON_SHOP_ITEMS.add(ItemID.HAMMER);
        COMMON_SHOP_ITEMS.add(ItemID.CHISEL);
        COMMON_SHOP_ITEMS.add(ItemID.TINDERBOX);
        COMMON_SHOP_ITEMS.add(ItemID.BUCKET);
        COMMON_SHOP_ITEMS.add(ItemID.JUG);
        COMMON_SHOP_ITEMS.add(ItemID.BOWL);
        COMMON_SHOP_ITEMS.add(ItemID.POT);
        COMMON_SHOP_ITEMS.add(ItemID.KNIFE);
        COMMON_SHOP_ITEMS.add(ItemID.ROPE);
        COMMON_SHOP_ITEMS.add(ItemID.NEEDLE);
        COMMON_SHOP_ITEMS.add(ItemID.THREAD);
        
        // Basic arrows and bolts
        COMMON_SHOP_ITEMS.add(ItemID.BRONZE_ARROW);
        COMMON_SHOP_ITEMS.add(ItemID.IRON_ARROW);
        COMMON_SHOP_ITEMS.add(ItemID.STEEL_ARROW);
        COMMON_SHOP_ITEMS.add(ItemID.BRONZE_BOLTS);
        COMMON_SHOP_ITEMS.add(ItemID.IRON_BOLTS);
        
        // Basic weapons and armor (low level)
        COMMON_SHOP_ITEMS.add(ItemID.BRONZE_DAGGER);
        COMMON_SHOP_ITEMS.add(ItemID.BRONZE_SWORD);
        COMMON_SHOP_ITEMS.add(ItemID.IRON_DAGGER);
        COMMON_SHOP_ITEMS.add(ItemID.IRON_SWORD);
        
        // Basic food
        COMMON_SHOP_ITEMS.add(ItemID.BREAD);
        COMMON_SHOP_ITEMS.add(ItemID.CABBAGE);
        COMMON_SHOP_ITEMS.add(ItemID.ONION);
        COMMON_SHOP_ITEMS.add(ItemID.POTATO);
        
        // Basic runes (magic shops)
        COMMON_SHOP_ITEMS.add(ItemID.AIR_RUNE);
        COMMON_SHOP_ITEMS.add(ItemID.WATER_RUNE);
        COMMON_SHOP_ITEMS.add(ItemID.EARTH_RUNE);
        COMMON_SHOP_ITEMS.add(ItemID.FIRE_RUNE);
        COMMON_SHOP_ITEMS.add(ItemID.MIND_RUNE);
        COMMON_SHOP_ITEMS.add(ItemID.BODY_RUNE);
    }
    
    private static void initializePlayerSoldItems() 
    {
        // High-value items commonly sold by players
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.RUNE_PLATEBODY);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.RUNE_PLATELEGS);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.RUNE_FULL_HELM);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.DRAGON_LONGSWORD);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.DRAGON_DAGGER);
        
        // Processed/cooked items
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.COOKED_CHICKEN);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.LOBSTER);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.SWORDFISH);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.SHARK);
        
        // Crafted items
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.LEATHER_BODY);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.LEATHER_CHAPS);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.GREEN_DHIDE_BODY);
        
        // Potions
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.STRENGTH_POTION4);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.ATTACK_POTION4);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.DEFENCE_POTION4);
        
        // Logs and ores (commonly gathered and sold)
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.YEW_LOGS);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.MAGIC_LOGS);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.COAL);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.IRON_ORE);
        COMMONLY_PLAYER_SOLD_ITEMS.add(ItemID.GOLD_ORE);
    }
    
    private static void initializeStaticSpawnItems() 
    {
        // Common static spawn items found throughout the world
        STATIC_SPAWN_ITEMS.add(ItemID.BRONZE_DAGGER);
        STATIC_SPAWN_ITEMS.add(ItemID.KNIFE);
        STATIC_SPAWN_ITEMS.add(ItemID.BREAD);
        STATIC_SPAWN_ITEMS.add(ItemID.BUCKET);
        STATIC_SPAWN_ITEMS.add(ItemID.JUG);
        STATIC_SPAWN_ITEMS.add(ItemID.BOWL);
        STATIC_SPAWN_ITEMS.add(ItemID.POT);
        STATIC_SPAWN_ITEMS.add(ItemID.CABBAGE);
        STATIC_SPAWN_ITEMS.add(ItemID.ONION);
        STATIC_SPAWN_ITEMS.add(ItemID.POTATO);
        STATIC_SPAWN_ITEMS.add(ItemID.TINDERBOX);
        STATIC_SPAWN_ITEMS.add(ItemID.HAMMER);
        STATIC_SPAWN_ITEMS.add(ItemID.CHISEL);
        STATIC_SPAWN_ITEMS.add(ItemID.ROPE);
        STATIC_SPAWN_ITEMS.add(ItemID.CANDLE);
        STATIC_SPAWN_ITEMS.add(ItemID.LOGS);
        STATIC_SPAWN_ITEMS.add(ItemID.ARROW_SHAFT);
        STATIC_SPAWN_ITEMS.add(ItemID.FEATHER);
    }
    
    /**
     * Checks if an item is likely to be original shop stock
     */
    public static boolean isLikelyShopItem(int itemId) 
    {
        return COMMON_SHOP_ITEMS.contains(itemId);
    }
    
    /**
     * Checks if an item is commonly sold by players to shops
     */
    public static boolean isCommonlyPlayerSold(int itemId) 
    {
        return COMMONLY_PLAYER_SOLD_ITEMS.contains(itemId);
    }
    
    /**
     * Checks if an item is a known static spawn
     */
    public static boolean isStaticSpawnItem(int itemId) 
    {
        return STATIC_SPAWN_ITEMS.contains(itemId);
    }
    
    /**
     * Analyzes an item to determine if it's likely original shop stock based on its properties
     */
    public static boolean analyzeItemForShopOrigin(Client client, int itemId) 
    {
        ItemComposition itemComp = client.getItemDefinition(itemId);
        if (itemComp == null) 
        {
            return false;
        }
        
        String itemName = itemComp.getName().toLowerCase();
        int value = itemComp.getPrice();
        
        // Check against known lists first
        if (isLikelyShopItem(itemId)) 
        {
            return true;
        }
        
        if (isCommonlyPlayerSold(itemId)) 
        {
            return false;
        }
        
        // Heuristic analysis
        return analyzeItemHeuristics(itemName, value);
    }
    
    private static boolean analyzeItemHeuristics(String itemName, int value) 
    {
        // Items with very high value are usually player-sold
        if (value > 10000) 
        {
            return false;
        }
        
        // Check for processed item indicators
        if (isProcessedItem(itemName)) 
        {
            return false;
        }
        
        // Check for basic supply indicators
        if (isBasicSupply(itemName)) 
        {
            return true;
        }
        
        // Check for rare/special items
        if (isRareItem(itemName)) 
        {
            return false;
        }
        
        // Default to shop item for uncertainty
        return true;
    }
    
    private static boolean isProcessedItem(String itemName) 
    {
        String[] processedKeywords = {
            "cooked", "roasted", "baked", "barbecued", "grilled",
            "potion", "brew", "mix", "dose", "draught",
            "enchanted", "magic", "blessed", "cursed",
            "crafted", "smithed", "carved", "cut", "polished",
            "refined", "pure", "super", "divine"
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
    
    private static boolean isBasicSupply(String itemName) 
    {
        String[] basicSupplyKeywords = {
            "arrow", "bolt", "needle", "thread", "string",
            "chisel", "hammer", "tinderbox", "knife",
            "bucket", "jug", "vial", "bowl", "pot",
            "rope", "candle", "torch", "lantern",
            "bronze", "iron", "steel", // Basic metal items
            "leather", "hide", // Basic materials
            "air rune", "water rune", "earth rune", "fire rune", "mind rune", "body rune"
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
    
    private static boolean isRareItem(String itemName) 
    {
        String[] rareKeywords = {
            "dragon", "rune", "adamant", "mithril", // Higher tier equipment
            "abyssal", "barrows", "crystal", "elven",
            "godsword", "whip", "dagger p++", "sword p++",
            "amulet of", "ring of", "necklace of",
            "clue", "casket", "scroll",
            "rare", "special", "unique"
        };
        
        for (String keyword : rareKeywords) 
        {
            if (itemName.contains(keyword)) 
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the display name for an item
     */
    public static String getItemName(Client client, int itemId) 
    {
        ItemComposition itemComp = client.getItemDefinition(itemId);
        return itemComp != null ? itemComp.getName() : "Unknown Item";
    }
    
    /**
     * Gets the value of an item
     */
    public static int getItemValue(Client client, int itemId) 
    {
        ItemComposition itemComp = client.getItemDefinition(itemId);
        return itemComp != null ? itemComp.getPrice() : 0;
    }
    
    /**
     * Checks if an item is tradeable
     */
    public static boolean isItemTradeable(Client client, int itemId) 
    {
        ItemComposition itemComp = client.getItemDefinition(itemId);
        return itemComp != null && itemComp.isTradeable();
    }
    
    /**
     * Checks if an item is noted
     */
    public static boolean isItemNoted(Client client, int itemId) 
    {
        ItemComposition itemComp = client.getItemDefinition(itemId);
        return itemComp != null && itemComp.getNote() != -1;
    }
}