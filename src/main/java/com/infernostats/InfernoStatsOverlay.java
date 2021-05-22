package com.infernostats;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import com.infernostats.wavehistory.Wave;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;

class InfernoStatsOverlay extends OverlayPanel
{
    private final Client client;
    private final InfernoStatsPlugin plugin;
    private final InfernoStatsConfig config;
    private static Wave prevSplitWave = null;

    private static final Color HEADER_COLOR = ColorScheme.BRAND_ORANGE;

    @Inject
    private InfernoStatsOverlay(Client client, InfernoStatsPlugin plugin, InfernoStatsConfig config)
    {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        final boolean inVoid = plugin.isInVoid();
        final boolean inInferno = plugin.isInInferno();

        if (!inVoid && !inInferno)
        {
            return null;
        }

        panelComponent.getChildren().clear();

        Wave wave = plugin.GetCurrentWave();
        if (wave == null)
        {
            return null;
        }

        if (wave.IsSplit())
        {
            prevSplitWave = wave;
        }

        String header = "Current Wave: " + wave.id;
        ArrayList<String> contents = new ArrayList<>();

        if (prevSplitWave != null)
        {
            if (config.splitTimes())
            {
                contents.add("Wave " + prevSplitWave.id + " Split: " + prevSplitWave.SplitTimeString());
            }

            if (config.predictedCompletionTime() && prevSplitWave.predictedTime != null)
            {
                contents.add("Predicted Time: " + prevSplitWave.PredictedTimeString());
            }
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(header)
                .color(HEADER_COLOR)
                .build());

        for (String content : contents)
        {
            panelComponent.getChildren().add(
                LineComponent
                    .builder()
                    .left(content)
                    .build());
        }

        return super.render(graphics);
    }

}