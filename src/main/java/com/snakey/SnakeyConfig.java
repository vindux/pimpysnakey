package com.snakey;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("example")
public interface SnakeyConfig extends Config {
	@ConfigItem(
			keyName = "flashColor",
			name = "Flash Color",
			description = "The color to flash the overlay",
			position = 1
	)
	default Color flashColor() {
		return new Color(255, 0, 0, 161);
	}
}
