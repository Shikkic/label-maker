package net.runelite.client.plugins.labelmaker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.plugins.labelmaker.YOLOAnnotation;
import net.runelite.client.util.ImageCapture;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.client.ui.DrawManager;
import java.util.UUID;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
        name = "Labelmaker"
)
public class LabelMakerPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private LabelMakerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private ImageCapture imageCapture;

    @Inject
    private DrawManager drawManager;

    private ObjectIDOverlay objectIDOverlay;

    private List<String> npcsToLabel = new CopyOnWriteArrayList<>();

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
            int screenshotWidth = client.getRealDimensions().width;
            int screenshotHeight = client.getRealDimensions().height;
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + screenshotHeight + " " + screenshotWidth, null);
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

        void takeScreenshot(List<YOLOAnnotation> annotations)
        {
            String fileNameUUID = UUID.randomUUID().toString();
            Consumer<Image> imageCallback = (img) ->
            {
                // This callback is on the game thread, move to executor thread
                executor.submit(() -> saveScreenshot(fileNameUUID, "labelmaker", img, annotations));
            };

            drawManager.requestNextFrameListener(imageCallback);
        }

        public void writeAnnotationsToFile(String imageName, List<YOLOAnnotation> annotations) {
            String filename = "C:\\Users\\shikkic\\Desktop\\dataset\\labels\\" + imageName + ".txt"; // Assuming image name is like 'image1.jpg'

            try (FileWriter writer = new FileWriter(filename)) {
                for (YOLOAnnotation annotation : annotations) {
                    writer.write(annotation.toString() + "\n"); // Uses the toString() method
                }
            } catch (IOException e) {
                System.err.println("Error writing annotations: " + e.getMessage());
            }
        }

        private void saveScreenshot(String fileName, String subDir, Image image, List<YOLOAnnotation> annotations)
        {
            final BufferedImage screenshot;

            screenshot = ImageUtil.bufferedImageFromImage(image);

            imageCapture.saveScreenshot(screenshot, fileName, subDir, false, false);
            writeAnnotationsToFile(fileName, annotations);
        }

        @Override
        public Dimension render(Graphics2D graphics) {
            npcsToLabel = Text.fromCSV(config.npcs());
            int screenshotWidth = client.getRealDimensions().width;
            int screenshotHeight = client.getRealDimensions().height;


            Player player = client.getLocalPlayer();
            if (player == null) {
                return null;
            }

            LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
            if (playerLocation == null)
            {
                return null;
            }

            List<NPC> npcs = client.getNpcs();
            List<YOLOAnnotation> annotations = new ArrayList<>();
            for (NPC npc : npcs) {
                Shape convexHull = npc.getConvexHull();
                if (convexHull == null) {
                    continue;
                }
                NPCComposition composition = npc.getComposition();
                Rectangle2D aabb = convexHull.getBounds2D();

                // Get bounding box coordinates
                int width = (int) aabb.getWidth();
                int height = (int) aabb.getHeight();

                int x = (int) aabb.getMinX() + (width / 2);
                int y = (int) aabb.getMinY() + (height / 2);

                int categoryID = npc.getId();
                String categoryIDstr = String.valueOf(categoryID);
                if (!npcsToLabel.contains(categoryIDstr)) {
                    continue;
                }
                if (x < 0 || y < 0) {
                    continue;
                }

                // These coordinates are for the in-game world I think and not reflective
                // of the coordinates in relation to player perspective / screenshot.
                int npcIndex = npcsToLabel.indexOf(categoryIDstr);
                annotations.add(new YOLOAnnotation(npcIndex, x, y, width, height, screenshotWidth, screenshotHeight));

                // Rendering AABB for debugging purposes.
                if (config.debugBBEnabled()) {
                    OverlayUtil.renderPolygon(graphics, aabb, Color.RED);
                }
            }

            if (config.screenshot()) {
                takeScreenshot(annotations);
            }

            return null;
        }
    }
}
