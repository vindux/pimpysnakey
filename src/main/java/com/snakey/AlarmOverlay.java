package com.snakey;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

import java.awt.*;

public class AlarmOverlay extends OverlayPanel {
	private final Client client;
	private final SnakeyConfig config;

	@Inject
	private AlarmOverlay(Client client, SnakeyConfig config) {
		this.client = client;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(client.getCanvasWidth(), client.getCanvasHeight()));
		for (int i = 0; i < 100; ++i) {
			panelComponent.getChildren().add((LineComponent.builder()).left(" ").build());
		}
		if (client.getGameCycle() % 20 >= 10) {
			panelComponent.setBackgroundColor(config.flashColor());
		}
		else {
			panelComponent.setBackgroundColor(new Color(0, 0, 0, 0));
		}
		return panelComponent.render(graphics);
	}
}