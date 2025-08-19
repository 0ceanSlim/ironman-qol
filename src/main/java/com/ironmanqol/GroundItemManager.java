package com.ironmanqol;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class GroundItemManager 
{
    @Inject
    private Client client;

    // Track ground items and their ownership
    private final Map<WorldPoint, Map<Integer, ItemOwnership>> groundItems = new ConcurrentHashMap<>();
    
    // Track player's recent item drops with timestamps
    private final Map<TileItem, Long> playerDropTimes = new ConcurrentHashMap<>();
    
    // Known static spawn locations and items
    private final Map<WorldPoint, Set<Integer>> staticSpawns = new ConcurrentHashMap<>();
    
    // NPCs recently killed by the player (for loot eligibility)
    private final Map<Integer, Long> recentPlayerKills = new ConcurrentHashMap<>();

    private static final long DROP_TIMEOUT = 60000; // 1 minute
    private static final long KILL_TIMEOUT = 10000; // 10 seconds

    public enum ItemOwnership 
    {
        PLAYER_DROPPED,
        PLAYER_LOOT,
        OTHER_PLAYER,
        STATIC_SPAWN,
        UNKNOWN
    }

    public void onItemSpawned(ItemSpawned event) 
    {
        TileItem item = event.getItem();
        Tile tile = event.getTile();
        WorldPoint worldPoint = tile.getWorldLocation();
        
        // Check if this is a known static spawn
        if (isStaticSpawn(worldPoint, item.getId())) 
        {
            addGroundItem(worldPoint, item.getId(), ItemOwnership.STATIC_SPAWN);
            return;
        }
        
        // Check if this might be loot from a recent player kill
        if (isRecentPlayerKillLoot(worldPoint)) 
        {
            addGroundItem(worldPoint, item.getId(), ItemOwnership.PLAYER_LOOT);
            return;
        }
        
        // Default to unknown/other player
        addGroundItem(worldPoint, item.getId(), ItemOwnership.OTHER_PLAYER);
    }

    public void onItemDespawned(ItemDespawned event) 
    {
        TileItem item = event.getItem();
        Tile tile = event.getTile();
        WorldPoint worldPoint = tile.getWorldLocation();
        
        removeGroundItem(worldPoint, item.getId());
        playerDropTimes.remove(item);
    }

    // Note: ItemDropped might not exist in RuneLite API, using alternative approach
    public void onItemContainerChanged(ItemContainerChanged event) 
    {
        // This is called when inventory changes - we can detect drops this way
        if (event.getContainerId() == InventoryID.INVENTORY.getId()) 
        {
            // Would need to track previous inventory state to detect drops
            // This is a simplified approach
        }
    }

    public void onActorDeath(ActorDeath event) 
    {
        Actor actor = event.getActor();
        if (actor instanceof NPC) 
        {
            NPC npc = (NPC) actor;
            Player localPlayer = client.getLocalPlayer();
            
            if (localPlayer != null && wasKilledByPlayer(npc, localPlayer)) 
            {
                // Track recent kills for loot eligibility
                int npcIndex = npc.getIndex();
                recentPlayerKills.put(npcIndex, System.currentTimeMillis());
                
                // Clean up old kills
                cleanupOldKills();
            }
        }
    }

    public boolean canPickUpItem(TileItem item, WorldPoint location) 
    {
        ItemOwnership ownership = getItemOwnership(location, item.getId());
        
        switch (ownership) 
        {
            case PLAYER_DROPPED:
            case PLAYER_LOOT:
            case STATIC_SPAWN:
                return true;
            case OTHER_PLAYER:
            case UNKNOWN:
            default:
                return false;
        }
    }

    public ItemOwnership getItemOwnership(WorldPoint location, int itemId) 
    {
        Map<Integer, ItemOwnership> locationItems = groundItems.get(location);
        if (locationItems != null) 
        {
            return locationItems.getOrDefault(itemId, ItemOwnership.UNKNOWN);
        }
        return ItemOwnership.UNKNOWN;
    }

    public void addStaticSpawn(WorldPoint location, int itemId) 
    {
        staticSpawns.computeIfAbsent(location, k -> ConcurrentHashMap.newKeySet()).add(itemId);
    }

    public void loadStaticSpawns() 
    {
        // Load known static spawn locations
        // This would ideally be loaded from a data file or resource
        // For now, adding some common examples
        
        // Lumbridge spawn items (examples)
        addStaticSpawn(new WorldPoint(3225, 3218, 0), ItemID.BRONZE_DAGGER); // Example
        addStaticSpawn(new WorldPoint(3207, 3212, 2), ItemID.KNIFE); // Example
        
        // Add more static spawns as needed
        // In a production plugin, this would load from a comprehensive database
    }

    private void addGroundItem(WorldPoint location, int itemId, ItemOwnership ownership) 
    {
        groundItems.computeIfAbsent(location, k -> new ConcurrentHashMap<>()).put(itemId, ownership);
    }

    private void removeGroundItem(WorldPoint location, int itemId) 
    {
        Map<Integer, ItemOwnership> locationItems = groundItems.get(location);
        if (locationItems != null) 
        {
            locationItems.remove(itemId);
            if (locationItems.isEmpty()) 
            {
                groundItems.remove(location);
            }
        }
    }

    private boolean isStaticSpawn(WorldPoint location, int itemId) 
    {
        Set<Integer> spawnItems = staticSpawns.get(location);
        return spawnItems != null && spawnItems.contains(itemId);
    }

    private boolean isRecentPlayerKillLoot(WorldPoint lootLocation) 
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) 
        {
            return false;
        }
        
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        
        // Check if loot is near player and we have recent kills
        if (playerLocation.distanceTo(lootLocation) <= 10 && !recentPlayerKills.isEmpty()) 
        {
            cleanupOldKills();
            return !recentPlayerKills.isEmpty();
        }
        
        return false;
    }

    private boolean wasKilledByPlayer(NPC npc, Player player) 
    {
        // Check various indicators that the player killed this NPC
        Actor target = npc.getInteracting();
        
        // Direct interaction check
        if (target == player) 
        {
            return true;
        }
        
        // Check if NPC was recently damaged by player
        // (This is a simplified check - more sophisticated tracking might be needed)
        return npc.getHealthRatio() == 0 && 
               player.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 10;
    }

    private void cleanupOldKills() 
    {
        long currentTime = System.currentTimeMillis();
        recentPlayerKills.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > KILL_TIMEOUT);
    }

    public void cleanup() 
    {
        long currentTime = System.currentTimeMillis();
        
        // Clean up old player drops
        playerDropTimes.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > DROP_TIMEOUT);
            
        cleanupOldKills();
    }

    public void reset() 
    {
        groundItems.clear();
        playerDropTimes.clear();
        recentPlayerKills.clear();
        // Don't clear static spawns as they're persistent
    }
}