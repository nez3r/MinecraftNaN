package net.minecraft.src;

import java.util.Random;
import net.minecraft.server.MinecraftServer;

public final class NaNManager {
	private static final int HORROR_EVENT = 2;
	private static final int EFFECT_RED_TEXT = 4;
	private static final int MIN_INTERVAL = 20 * 60 * 4;
	private static final int MAX_INTERVAL = 20 * 60 * 8;
	private final MinecraftServer server;
	private final Random random = new Random();
	private int[] ticksUntilEvent = new int[0];
	private int nextEffect = 2;

	public NaNManager(MinecraftServer server) {
		this.server = server;
	}

	public void tick() {
		if(this.server.configManager == null) return;
		if(this.server.worldMngr == null) return;
		if(this.ticksUntilEvent.length != this.server.worldMngr.length) {
			this.ticksUntilEvent = new int[this.server.worldMngr.length];
			for(int i = 0; i < this.ticksUntilEvent.length; ++i) this.ticksUntilEvent[i] = nextInterval();
		}
		for(int i = 0; i < this.server.worldMngr.length; ++i) {
			WorldServer world = this.server.worldMngr[i];
			if(world == null || --this.ticksUntilEvent[i] > 0) continue;
			if(!world.playerEntities.isEmpty()) {
				int effect = this.nextEffect;
				int duration = effectDuration(effect);
				this.server.configManager.sendPacketToAllPlayersInDimension(
					new Packet201HorrorEvent(HORROR_EVENT, effect, duration), world.worldProvider.worldType);
				this.nextEffect = this.nextEffect == EFFECT_RED_TEXT ? 2 : this.nextEffect + 1;
			}
			this.ticksUntilEvent[i] = nextInterval();
		}
	}

	private int nextInterval() {
		return MIN_INTERVAL + this.random.nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
	}

	public String debugCommand(String command) {
		String[] parts = command.trim().split(" ");
		if(parts.length == 0) return null;
		if("mst".equalsIgnoreCase(parts[0])) {
			return "Next NaN event: " + effectName(this.nextEffect) + " (random order: 4-8 minutes)";
		}
		if("next".equalsIgnoreCase(parts[0])) {
			this.startForAll(this.nextEffect);
			String result = "Started NaN event: " + effectName(this.nextEffect);
			this.nextEffect = this.nextEffect == EFFECT_RED_TEXT ? 2 : this.nextEffect + 1;
			return result;
		}
		if("event".equalsIgnoreCase(parts[0]) && parts.length > 1) {
			int effect = effectId(parts[1]);
			if(effect == 0) return "Unknown NaN event: " + parts[1];
			this.startForAll(effect);
			return "Started NaN event: " + effectName(effect);
		}
		return "Usage: /mst, /event <name>, /next";
	}

	private void startForAll(int effect) {
		this.server.configManager.sendPacketToAllPlayers(new Packet201HorrorEvent(HORROR_EVENT, effect, effectDuration(effect)));
	}

	private String effectName(int effect) {
		switch(effect) {
		case 2: return "voxel";
		case 3: return "bleed";
		case EFFECT_RED_TEXT: return "redtext";
		default: return "unknown";
		}
	}

	private int effectId(String name) {
		if("voxel".equalsIgnoreCase(name) || "collapse".equalsIgnoreCase(name)) return 2;
		if("bleed".equalsIgnoreCase(name) || "wireframe".equalsIgnoreCase(name)) return 3;
		if("redtext".equalsIgnoreCase(name) || "red".equalsIgnoreCase(name)) return EFFECT_RED_TEXT;
		return 0;
	}

	private int effectDuration(int effect) {
		if(effect == 3) return 20 * 7;
		if(effect == EFFECT_RED_TEXT) return 20 * 15;
		return 20 * 5;
	}
}
