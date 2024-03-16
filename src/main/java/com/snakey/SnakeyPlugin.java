package com.snakey;

import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Slf4j
@PluginDescriptor(name = "<html><body style = 'color:#ff0000'>P<span style = 'color:ff8000'>i<span style = 'color:ffff00'>m" +
		"<span style = 'color:80ff00'>p<span style = 'color:00ffff'>y<span style = 'color:0080ff'> <span style = 'color:8000ff'>S" +
		"<span style = 'color:ff0080'>n<span style = 'color:white'>a<span style = 'color:00ff00'>k<span style = 'color:ff0000'>e" +
		"<span style = 'color:0000ff'>y<span style = 'color:white'>.")
public class SnakeyPlugin extends Plugin {
	@Inject private ChatMessageManager chatMessageManager;
	@Inject private OverlayManager overlayManager;
	@Inject private AlarmOverlay overlay;
	@Inject private Client client;
	private int aabbCenterX = -1;
	private boolean overlayOn;
	private int onTick = 0;

	@Override
	protected void startUp() {
		aabbCenterX = -1;
		onTick = 0;
		setLogoutTimer(Integer.MAX_VALUE);
	}

	@Override
	protected void shutDown() {
		aabbCenterX = -1;
		onTick = 0;
		if (overlayOn) {
			overlayOn = false;
			overlayManager.remove(overlay);
		}
		setLogoutTimer(15000);
	}

	@SneakyThrows
	private void setLogoutTimer(int clientTicks) {
		client.setIdleTimeout(42069);
		for (Field declaredField : client.getClass().getDeclaredFields()) {
			if (declaredField.getType() == int.class && Modifier.isStatic(declaredField.getModifiers())) {
				declaredField.setAccessible(true);
				int value = declaredField.getInt(null);
				if (value != 42069) {
					declaredField.setAccessible(false);
					continue;
				}
				declaredField.setInt(null, clientTicks);
				declaredField.setAccessible(false);
				log.info("Idle time = " + client.getIdleTimeout());
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (findSnakey()) {
			if (!overlayOn) {
				onTick = client.getTickCount();
				overlayOn = true;
				overlayManager.add(overlay);
			}
		}

		if (overlayOn && (client.getTickCount() - onTick) > 10) {
			overlayOn = false;
			overlayManager.remove(overlay);
		}
	}

	private boolean findSnakey() {
		for (Tile[] tiles : client.getScene().getTiles()[client.getPlane()]) {
			if (tiles == null) continue;

			for (Tile tile : tiles) {
				if (tile == null) continue;
				for (GameObject gameObject : tile.getGameObjects()) {
					if (gameObject == null) continue;
					if (gameObject.getId() != 3193) continue;
					return (checkSnakeModel(gameObject));
				}
			}
		}
		return false;
	}

	private boolean checkSnakeModel(GameObject gameObject) {
		Model m = gameObject.getRenderable().getModel();
		if (m == null) return false;

		AABB aabb = m.getAABB(gameObject.getOrientation());
		if (aabbCenterX == -1) {
			aabbCenterX = aabb.getCenterX();
			sendGameMessage("Initial AABB cx: " + aabb.getCenterX());
		}
		else {
			if (aabb.getCenterX() > (aabbCenterX + 20)) {
				sendGameMessage("aabb cx: " + aabb.getCenterX() + " aabbCenterX: " + aabbCenterX);
				sendGameMessage("SOMEONE ENTERED THE SNAKE");
				return true;
			}
		}
		return false;
	}

	private void sendGameMessage(String message) {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		String chatMessage = new ChatMessageBuilder().append(Color.red, message).build();
		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());
	}

	@Provides
	SnakeyConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(SnakeyConfig.class);
	}
}
