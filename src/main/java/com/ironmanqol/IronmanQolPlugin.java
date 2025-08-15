package com.ironmanqol;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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

	@Override
	protected void startUp() throws Exception
	{
		log.info("Ironman QoL plugin started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Ironman QoL plugin stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// Plugin initialization when player logs in
		}
	}

	@Provides
	IronmanQolConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronmanQolConfig.class);
	}
}