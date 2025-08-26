package com.ironmanqol;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
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
    // Debug output to verify class loading
    static {
        System.out.println("=== IRONMAN QOL PLUGIN CLASS LOADED ===");
    }
    @Inject
    private Client client;

    @Inject
    private IronmanQolConfig config;

    // Remove manager injections for now - we'll add them back later
    // @Inject
    // private GroundItemManager groundItemManager;
    // @Inject
    // private ShopManager shopManager;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Ironman QoL plugin started!");
        System.out.println("=== IRONMAN QOL PLUGIN STARTED ===");

        // Simple initialization without managers for now
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            initializePlugin();
        }
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("Ironman QoL plugin stopped!");
        System.out.println("=== IRONMAN QOL PLUGIN STOPPED ===");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            log.info("Player logged in - initializing plugin");
            initializePlugin();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!isIronman())
        {
            return;
        }

        // Simple tick counter for debugging
        if (client.getTickCount() % 100 == 0)
        {
            log.debug("Plugin active - tick: {}", client.getTickCount());
        }
    }

    private void initializePlugin()
    {
        log.info("Initializing Ironman QoL plugin");
        System.out.println("Initializing plugin for player: " +
                (client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "Unknown"));

        if (isIronman())
        {
            log.info("Ironman detected - plugin fully active");
            System.out.println("IRONMAN DETECTED - Plugin active!");
        }
        else
        {
            log.info("Non-ironman account - plugin will have limited functionality");
            System.out.println("Non-ironman account detected");
        }
    }

    private boolean isIronman()
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null)
        {
            return false;
        }

        // Use account type to detect ironman status
        //AccountType accountType = client.getAccountType();
        //if (accountType != null)
        //{
        //    return accountType == AccountType.IRONMAN ||
        //            accountType == AccountType.HARDCORE_IRONMAN ||
        //            accountType == AccountType.ULTIMATE_IRONMAN;
        //}

        // Fallback: Use player name suffix to detect ironman status
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

    @Provides
    IronmanQolConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(IronmanQolConfig.class);
    }
}