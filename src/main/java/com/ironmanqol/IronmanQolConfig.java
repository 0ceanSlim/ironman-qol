package com.ironmanqol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ironmanqol")
public interface IronmanQolConfig extends Config
{
	@ConfigItem(
		keyName = "hideGroundItems",
		name = "Hide Inaccessible Ground Items",
		description = "Hide ground items that ironmen cannot pick up"
	)
	default boolean hideGroundItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOwnDrops",
		name = "Show Own Drops",
		description = "Always show items dropped by the player"
	)
	default boolean showOwnDrops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showStaticSpawns",
		name = "Show Static Spawns",
		description = "Always show world spawn items"
	)
	default boolean showStaticSpawns()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideShopItems",
		name = "Hide Inaccessible Shop Items",
		description = "Hide shop items that ironmen cannot purchase"
	)
	default boolean hideShopItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOriginalStock",
		name = "Show Original Shop Stock",
		description = "Always show items originally sold by the shop"
	)
	default boolean showOriginalStock()
	{
		return true;
	}

	@ConfigItem(
		keyName = "removeClickOptions",
		name = "Remove Click Options",
		description = "Remove inaccessible items from left-click options"
	)
	default boolean removeClickOptions()
	{
		return true;
	}
}