package net.runelite.client.plugins.labelmaker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.*;

import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@PluginDescriptor(
        name = "Example"
)
public class LabelMakerPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private LabelMakerConfig config;

    @Inject
    private OverlayManager overlayManager;

    private ObjectIDOverlay objectIDOverlay;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Example started!");

        objectIDOverlay = new ObjectIDOverlay(this);
        overlayManager.add(objectIDOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("Example stopped!");
        overlayManager.remove(objectIDOverlay);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
        }
    }

    @Provides
    LabelMakerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LabelMakerConfig.class);
    }

    private class ObjectIDOverlay extends Overlay {

        private final LabelMakerPlugin plugin;

        public ObjectIDOverlay(LabelMakerPlugin plugin) {
            this.plugin = plugin;
            setPosition(OverlayPosition.DYNAMIC);
            setPriority(OverlayPriority.HIGH);
        }

        @Override
        public Dimension render(Graphics2D graphics) {
            Scene scene = client.getScene();
            for (final Tile[] tiles : client.getScene().getTiles()[client.getPlane()])
            {
                for (final Tile tile : tiles) {
                    if (tile == null) {
                        continue;
                    }

                    for (final GameObject object : tile.getGameObjects()) {
                        if (object == null) {
                            continue;
                        }
                        if (object.getRenderable() instanceof Model) {
                            Shape convexHull = object.getConvexHull();
                            if (convexHull != null)
                            {
                                OverlayUtil.renderPolygon(graphics, convexHull, Color.RED);
                            }

                        }
                    }
                }
            }
            return null;
        }
    }
}
