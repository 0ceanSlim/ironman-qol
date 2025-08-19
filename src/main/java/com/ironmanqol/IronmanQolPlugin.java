package com.ironmanqol;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Ironman QoL",
	description = "Quality of life improvements for ironman accounts - hides inaccessible ground items and shop items",
	tags = {"ironman", "qol", "ground items", "shops", "filter"}
)
public class IronmanQolPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IronmanQolConfig config;

	@Inject
	private GroundItemManager groundItemManager;

	@Inject
	private ShopManager shopManager;

	@Override
	protected void startUp() throws Exception
	{
		System.out.println("Ironman QoL plugin started!");
		
		// Initialize managers
		groundItemManager.loadStaticSpawns();
		shopManager.loadKnownShopData();
		
		// Initialize if already logged in
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			initializePlugin();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		System.out.println("Ironman QoL plugin stopped!");
		groundItemManager.reset();
		shopManager.reset();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			initializePlugin();
		}
		else if (event.getGameState() == GameState.HOPPING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			// Clear tracked data when changing worlds or logging out
			groundItemManager.reset();
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (!isIronman())
		{
			return;
		}

		groundItemManager.onItemSpawned(event);
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		if (!isIronman())
		{
			return;
		}

		groundItemManager.onItemDespawned(event);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!isIronman())
		{
			return;
		}

		groundItemManager.onItemContainerChanged(event);
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (!isIronman())
		{
			return;
		}

		groundItemManager.onActorDeath(event);
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (!isIronman() || !config.removeClickOptions())
		{
			return;
		}

		MenuEntry[] entries = event.getMenuEntries();
		List<MenuEntry> filteredEntries = new ArrayList<>();

		for (MenuEntry entry : entries)
		{
			if (shouldShowMenuEntry(entry))
			{
				filteredEntries.add(entry);
			}
		}

		if (filteredEntries.size() != entries.length)
		{
			client.setMenuEntries(filteredEntries.toArray(new MenuEntry[0]));
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (!isIronman())
		{
			return;
		}

		// Handle shop interface updates
		if (event.getGroupId() == InterfaceID.SHOP)
		{
			shopManager.onWidgetLoaded(event);
			
			if (config.hideShopItems())
			{
				filterShopItems();
			}
		}
	}

	// Periodic cleanup task
	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isIronman())
		{
			return;
		}

		// Perform periodic cleanup every 100 ticks (1 minute)
		if (client.getTickCount() % 100 == 0)
		{
			groundItemManager.cleanup();
			shopManager.cleanup();
		}
	}

	private void initializePlugin()
	{
		System.out.println("Initializing Ironman QoL plugin for logged in player");
		// Any initialization needed when player logs in
	}

	private boolean isIronman()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null) 
		{
			return false;
		}
		
		// Use player name suffix to detect ironman status
		String playerName = localPlayer.getName();
		if (playerName != null) 
		{
			// Check for ironman crown symbols in name
			return playerName.contains("♦") || // Ironman symbol
				   playerName.contains("♠") || // Hardcore ironman symbol  
				   playerName.contains("♣");   // Ultimate ironman symbol
		}
		
		return false;
	}

	private boolean shouldShowMenuEntry(MenuEntry entry)
	{
		String option = entry.getOption();
		String target = entry.getTarget();

		// Filter ground item interactions
		if (option.equals("Take") && config.hideGroundItems())
		{
			return canPickUpGroundItem(entry);
		}

		// Filter shop interactions
		if ((option.equals("Buy") || option.equals("Buy-1") || option.equals("Buy-5") || 
			 option.equals("Buy-10") || option.equals("Buy-50")) && config.hideShopItems())
		{
			return canBuyShopItem(entry);
		}

		return true;
	}

	private boolean canPickUpGroundItem(MenuEntry entry)
	{
		// Extract tile item information from menu entry
		TileItem tileItem = getTileItemFromMenuEntry(entry);
		
		if (tileItem == null)
		{
			return true; // Default to showing if we can't determine
		}

		// Use ground item manager to determine if item can be picked up
		return groundItemManager.canPickUpItem(tileItem, getTileLocation(entry));
	}

	private boolean canBuyShopItem(MenuEntry entry)
	{
		int itemId = getShopItemIdFromMenuEntry(entry);
		int shopId = shopManager.getCurrentShopId();

		if (itemId == -1 || shopId == -1)
		{
			return true; // Default to showing if we can't determine
		}

		return shopManager.canBuyItem(shopId, itemId);
	}

	private TileItem getTileItemFromMenuEntry(MenuEntry entry)
	{
		// Extract TileItem from menu entry
		// This is a complex operation that requires parsing menu entry parameters
		int param0 = entry.getParam0();
		int param1 = entry.getParam1();
		
		Scene scene = client.getTopLevelWorldView().getScene();
		if (scene == null)
		{
			return null;
		}

		// The exact implementation depends on how RuneLite encodes tile coordinates
		// in menu entries. This is a simplified version.
		int plane = client.getTopLevelWorldView().getPlane();
		Tile[][][] tiles = scene.getTiles();
		
		if (param0 >= 0 && param0 < 104 && param1 >= 0 && param1 < 104)
		{
			Tile tile = tiles[plane][param0][param1];
			if (tile != null)
			{
				List<TileItem> tileItems = tile.getGroundItems();
				if (tileItems != null && !tileItems.isEmpty())
				{
					// Return the first item for simplicity
					// In practice, you'd need to match the specific item from the menu
					return tileItems.get(0);
				}
			}
		}
		
		return null;
	}

	private WorldPoint getTileLocation(MenuEntry entry)
	{
		int param0 = entry.getParam0();
		int param1 = entry.getParam1();
		int plane = client.getTopLevelWorldView().getPlane();
		
		// Convert scene coordinates to world coordinates
		WorldView worldView = client.getTopLevelWorldView();
		int baseX = worldView.getBaseX();
		int baseY = worldView.getBaseY();
		
		return new WorldPoint(baseX + param0, baseY + param1, plane);
	}

	private int getShopItemIdFromMenuEntry(MenuEntry entry)
	{
		// Extract item ID from shop menu entry
		// The item ID is typically encoded in the menu entry parameters
		int param1 = entry.getParam1();
		
		// For shop interactions, param1 often contains the item ID
		// This may need adjustment based on actual RuneLite menu encoding
		return param1;
	}

	private void filterShopItems()
	{
		Widget shopInventory = client.getWidget(InterfaceID.SHOP, 2);
		if (shopInventory == null)
		{
			return;
		}

		Widget[] shopItems = shopInventory.getChildren();
		if (shopItems == null)
		{
			return;
		}

		int shopId = shopManager.getCurrentShopId();
		
		for (Widget item : shopItems)
		{
			if (item != null && item.getItemId() != -1)
			{
				boolean shouldHide = shopManager.shouldHideShopItem(shopId, item.getItemId());
				item.setHidden(shouldHide);
			}
		}
	}

	@Provides
	IronmanQolConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronmanQolConfig.class);
	}
}