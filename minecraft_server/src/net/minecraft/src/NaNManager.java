package net.minecraft.src;

import java.util.Random;
import net.minecraft.server.MinecraftServer;

public final class NaNManager {
	private static final int HORROR_EVENT = 2;
	private static final int EFFECT_RED_TEXT = 4;
	private static final int EFFECT_INVENTORY_CORRUPTION = 5;
	private static final int EFFECT_RANDOM_LOOT = 6;
	private static final int EFFECT_RED_BARS = 7;
	private static final int MIN_INTERVAL = 20 * 60 * 4;
	private static final int MAX_INTERVAL = 20 * 60 * 8;
	private final MinecraftServer server;
	private final Random random = new Random();
	private int[] ticksUntilEvent = new int[0];
	private int nextEffect = EFFECT_RANDOM_LOOT;
	private boolean permanentEventStarted;
	private int[] redBarsTicks = new int[0];

	public NaNManager(MinecraftServer server) {
		this.server = server;
	}

	public void tick() {
		if(this.server.configManager == null || this.permanentEventStarted) return;
		if(this.server.worldMngr == null) return;
		if(this.ticksUntilEvent.length != this.server.worldMngr.length) {
			this.ticksUntilEvent = new int[this.server.worldMngr.length];
			this.redBarsTicks = new int[this.server.worldMngr.length];
			for(int i = 0; i < this.ticksUntilEvent.length; ++i) this.ticksUntilEvent[i] = nextInterval();
		}
		for(int i = 0; i < this.server.worldMngr.length; ++i) {
			WorldServer world = this.server.worldMngr[i];
			if(world == null) continue;
			if(this.redBarsTicks[i] > 0 && --this.redBarsTicks[i] == 0) {
				world.setWorldTime(world.getWorldTime() - world.getWorldTime() % 24000L + 13000L);
			}
			if(--this.ticksUntilEvent[i] > 0) continue;
			if(!world.playerEntities.isEmpty()) {
				int effect = this.nextEffect;
				int duration = effectDuration(effect);
				int itemId = effect == EFFECT_RANDOM_LOOT ? randomItemId() : -1;
				int itemCount = effect == EFFECT_RANDOM_LOOT ? randomItemCount(itemId) : 0;
				if(effect == EFFECT_RANDOM_LOOT) giveItemToPlayers(itemId, itemCount, world);
				if(effect == EFFECT_RED_BARS) this.redBarsTicks[i] = duration;
				this.server.configManager.sendPacketToAllPlayersInDimension(
					new Packet201HorrorEvent(HORROR_EVENT, effect, duration, itemId, itemCount), world.worldProvider.worldType);
				this.nextEffect = nextEffectId(this.nextEffect);
				if(effect == EFFECT_INVENTORY_CORRUPTION) this.permanentEventStarted = true;
			}
			this.ticksUntilEvent[i] = this.nextEffect == EFFECT_INVENTORY_CORRUPTION ? 20 * 60 * 2 : nextInterval();
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
			if(this.nextEffect == EFFECT_INVENTORY_CORRUPTION) this.permanentEventStarted = true;
			this.nextEffect = nextEffectId(this.nextEffect);
			return result;
		}
		if("event".equalsIgnoreCase(parts[0]) && parts.length > 1) {
			int effect = effectId(parts[1]);
			if(effect == 0) return "Unknown NaN event: " + parts[1];
			this.startForAll(effect);
			if(effect == EFFECT_INVENTORY_CORRUPTION) this.permanentEventStarted = true;
			return "Started NaN event: " + effectName(effect);
		}
		return "Usage: /mst, /event <name>, /next";
	}

	private void startForAll(int effect) {
		int itemId = effect == EFFECT_RANDOM_LOOT ? randomItemId() : -1;
		int itemCount = effect == EFFECT_RANDOM_LOOT ? randomItemCount(itemId) : 0;
		if(effect == EFFECT_RANDOM_LOOT) giveItemToPlayers(itemId, itemCount, null);
		if(effect == EFFECT_RED_BARS) {
			if(this.redBarsTicks.length != this.server.worldMngr.length) this.redBarsTicks = new int[this.server.worldMngr.length];
			for(int i = 0; i < this.redBarsTicks.length; ++i) this.redBarsTicks[i] = effectDuration(effect);
		}
		this.server.configManager.sendPacketToAllPlayers(new Packet201HorrorEvent(HORROR_EVENT, effect, effectDuration(effect), itemId, itemCount));
	}

	private String effectName(int effect) {
		switch(effect) {
		case 2: return "voxel";
		case 3: return "bleed";
		case EFFECT_RED_TEXT: return "redtext";
		case EFFECT_INVENTORY_CORRUPTION: return "inventory";
		case EFFECT_RANDOM_LOOT: return "loot";
		case EFFECT_RED_BARS: return "redbars";
		default: return "unknown";
		}
	}

	private int effectId(String name) {
		if("voxel".equalsIgnoreCase(name) || "collapse".equalsIgnoreCase(name)) return 2;
		if("bleed".equalsIgnoreCase(name) || "wireframe".equalsIgnoreCase(name)) return 3;
		if("redtext".equalsIgnoreCase(name) || "red".equalsIgnoreCase(name)) return EFFECT_RED_TEXT;
		if("inventory".equalsIgnoreCase(name) || "items".equalsIgnoreCase(name)) return EFFECT_INVENTORY_CORRUPTION;
		if("loot".equalsIgnoreCase(name) || "item".equalsIgnoreCase(name)) return EFFECT_RANDOM_LOOT;
		if("redbars".equalsIgnoreCase(name) || "bars".equalsIgnoreCase(name)) return EFFECT_RED_BARS;
		return 0;
	}

	private int effectDuration(int effect) {
		if(effect == 3) return 20 * 7;
		if(effect == EFFECT_RED_TEXT) return 20 * 15;
		if(effect == EFFECT_INVENTORY_CORRUPTION) return 0;
		if(effect == EFFECT_RANDOM_LOOT) return 0;
		if(effect == EFFECT_RED_BARS) return 246;
		return 20 * 5;
	}

	private int nextEffectId(int effect) {
		if(effect == EFFECT_RANDOM_LOOT) return 2;
		if(effect == EFFECT_RED_TEXT) return EFFECT_RED_BARS;
		if(effect == EFFECT_RED_BARS) return EFFECT_INVENTORY_CORRUPTION;
		if(effect == EFFECT_INVENTORY_CORRUPTION) return EFFECT_RANDOM_LOOT;
		return effect + 1;
	}

	private int randomItemId() {
		int itemId;
		do {
			itemId = this.random.nextInt(Item.itemsList.length);
		} while(Item.itemsList[itemId] == null);
		return itemId;
	}

	private int randomItemCount(int itemId) {
		ItemStack item = new ItemStack(itemId, 1, 0);
		return item.isStackable() ? this.random.nextInt(32) + 1 : 1;
	}

	private void giveItemToPlayers(int itemId, int itemCount, WorldServer world) {
		java.util.List players = world == null ? this.server.configManager.playerEntities : world.playerEntities;
		for(int i = 0; i < players.size(); ++i) {
			((EntityPlayerMP)players.get(i)).inventory.addItemStackToInventory(new ItemStack(itemId, itemCount, 0));
		}
	}
}
