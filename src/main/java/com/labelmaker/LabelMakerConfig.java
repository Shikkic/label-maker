package net.runelite.client.plugins.labelmaker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("labelmaker")
public interface LabelMakerConfig extends Config
{
    @ConfigItem(
            keyName = "greeting",
            name = "Welcome Greeting",
            description = "The message to show to the user when they login"
    )
    default String greeting()
    {
        return "Hello";
    }

    @ConfigItem(
            keyName = "debug bounding box",
            name = "Show bounding boxes",
            description = "TODO"
    )
    default boolean debugBBEnabled()
    {
        return false;
    }

    @ConfigItem(
            keyName = "take screenshot",
            name = "TODO",
            description = "TODO"
    )
    default boolean screenshot()
    {
        return false;
    }

    @ConfigItem(
            keyName = "nps",
            name = "NPCs to label",
            description = "TODo"
    )
    default String npcs()
    {
        return "";
    }
}
