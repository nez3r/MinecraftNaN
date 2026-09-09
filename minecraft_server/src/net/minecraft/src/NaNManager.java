package net.minecraft.src;

import java.util.Random;
import net.minecraft.server.MinecraftServer;

public final class NaNManager {
	private static final int HORROR_EVENT = 2;
	private static final int EFFECT_RED_TEXT = 4;
	private static final int EFFECT_INVENTORY_CORRUPTION = 5;
	private static final int EFFECT_RANDOM_LOOT = 6;
	private static final int EFFECT_RED_BARS = 7;
	private static final int EFFECT_BLOCK_EVENT = 8;
	private static final int EFFECT_COBWEB = 9;
	private static final int EFFECT_INSULTS = 10;
	private static final int EFFECT_INVENTORY_SHUFFLE = 11;
	private static final int EFFECT_MINIMAL_RENDER = 12;
	private static final int EFFECT_SIGN_SPAWN = 13;
	private static final int EFFECT_SCREEN_INVERSION = 14;
	private static final int EFFECT_DROP_ACTIVE = 15;
	private static final int EFFECT_WINDOW_SHAKE = 16;
	private static final int EFFECT_FAKE_ERROR = 17;
	private static final int EFFECT_CHAT_SPAM = 18;
	private static final int MIN_INTERVAL = 20 * 60 * 4;
	private static final int MAX_INTERVAL = 20 * 60 * 8;
	private final MinecraftServer server;
	private final Random random = new Random();
	private int[] ticksUntilEvent = new int[0];
	private int nextEffect = EFFECT_RANDOM_LOOT;
	private int eventSequence;
	private int lastConstrainedEffect;
	private int remainingBlockEvents;
	private int remainingCobwebEvents;
	private int remainingInsultEvents;
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
				applyPlayerEvent(effect, world);
				if(effect == EFFECT_RED_BARS) this.redBarsTicks[i] = duration;
				this.server.configManager.sendPacketToAllPlayersInDimension(
					new Packet201HorrorEvent(HORROR_EVENT, effect, duration, itemId, itemCount, ++this.eventSequence, eventMessage(effect)), world.worldProvider.worldType);
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
		applyPlayerEvent(effect, null);
		if(effect == EFFECT_RED_BARS) {
			if(this.redBarsTicks.length != this.server.worldMngr.length) this.redBarsTicks = new int[this.server.worldMngr.length];
			for(int i = 0; i < this.redBarsTicks.length; ++i) this.redBarsTicks[i] = effectDuration(effect);
		}
		this.server.configManager.sendPacketToAllPlayers(new Packet201HorrorEvent(HORROR_EVENT, effect, effectDuration(effect), itemId, itemCount, ++this.eventSequence, eventMessage(effect)));
	}

	private String effectName(int effect) {
		switch(effect) {
		case 2: return "voxel";
		case 3: return "bleed";
		case EFFECT_RED_TEXT: return "redtext";
		case EFFECT_INVENTORY_CORRUPTION: return "inventory";
		case EFFECT_RANDOM_LOOT: return "loot";
		case EFFECT_RED_BARS: return "redbars";
		case EFFECT_BLOCK_EVENT: return "block";
		case EFFECT_COBWEB: return "cobweb";
		case EFFECT_INSULTS: return "insults";
		case EFFECT_INVENTORY_SHUFFLE: return "shuffle";
		case EFFECT_MINIMAL_RENDER: return "minimal";
		case EFFECT_SIGN_SPAWN: return "sign";
		case EFFECT_SCREEN_INVERSION: return "invert";
		case EFFECT_DROP_ACTIVE: return "drop";
		case EFFECT_WINDOW_SHAKE: return "shake";
		case EFFECT_FAKE_ERROR: return "error";
		case EFFECT_CHAT_SPAM: return "chatspam";
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
		if("block".equalsIgnoreCase(name) || "blocks".equalsIgnoreCase(name)) return EFFECT_BLOCK_EVENT;
		if("cobweb".equalsIgnoreCase(name) || "web".equalsIgnoreCase(name)) return EFFECT_COBWEB;
		if("insult".equalsIgnoreCase(name) || "insults".equalsIgnoreCase(name)) return EFFECT_INSULTS;
		if("shuffle".equalsIgnoreCase(name) || "inventoryshuffle".equalsIgnoreCase(name)) return EFFECT_INVENTORY_SHUFFLE;
		if("minimal".equalsIgnoreCase(name) || "minimalrender".equalsIgnoreCase(name)) return EFFECT_MINIMAL_RENDER;
		if("sign".equalsIgnoreCase(name) || "signspawn".equalsIgnoreCase(name)) return EFFECT_SIGN_SPAWN;
		if("invert".equalsIgnoreCase(name) || "inversion".equalsIgnoreCase(name)) return EFFECT_SCREEN_INVERSION;
		if("drop".equalsIgnoreCase(name) || "dropactive".equalsIgnoreCase(name)) return EFFECT_DROP_ACTIVE;
		if("shake".equalsIgnoreCase(name) || "windowshake".equalsIgnoreCase(name)) return EFFECT_WINDOW_SHAKE;
		if("error".equalsIgnoreCase(name) || "fakeerror".equalsIgnoreCase(name)) return EFFECT_FAKE_ERROR;
		if("chatspam".equalsIgnoreCase(name) || "spam".equalsIgnoreCase(name)) return EFFECT_CHAT_SPAM;
		return 0;
	}

	private int effectDuration(int effect) {
		if(effect == 3) return 20 * 7;
		if(effect == EFFECT_RED_TEXT) return 20 * 15;
		if(effect == EFFECT_INVENTORY_CORRUPTION) return 0;
		if(effect == EFFECT_RANDOM_LOOT) return 0;
		if(effect == EFFECT_RED_BARS) return 246;
		if(effect == EFFECT_BLOCK_EVENT) return 20 * 5;
		if(effect == EFFECT_COBWEB) return 20 * 8;
		if(effect == EFFECT_INSULTS) return 20 * 4;
		if(effect == EFFECT_INVENTORY_SHUFFLE || effect == EFFECT_SIGN_SPAWN || effect == EFFECT_DROP_ACTIVE) return 1;
		if(effect == EFFECT_MINIMAL_RENDER) return 20 * 20;
		if(effect == EFFECT_SCREEN_INVERSION) return 20 * 8;
		if(effect == EFFECT_WINDOW_SHAKE) return 20 * 10;
		if(effect == EFFECT_FAKE_ERROR) return 20 * 12;
		if(effect == EFFECT_CHAT_SPAM) return 20 * 10;
		return 20 * 5;
	}

	private int nextEffectId(int effect) {
		if(effect == EFFECT_RANDOM_LOOT) return 2;
		if(effect == EFFECT_RED_TEXT) return EFFECT_RED_BARS;
		if(effect == EFFECT_RED_BARS) return EFFECT_INVENTORY_CORRUPTION;
		if(effect == EFFECT_INVENTORY_CORRUPTION) return nextConstrainedEffect();
		if(effect == EFFECT_BLOCK_EVENT || effect == EFFECT_COBWEB || effect == EFFECT_INSULTS) {
			return this.remainingBlockEvents == 0 && this.remainingCobwebEvents == 0 && this.remainingInsultEvents == 0
				? EFFECT_INVENTORY_SHUFFLE : nextConstrainedEffect();
		}
		if(effect == EFFECT_FAKE_ERROR) return EFFECT_CHAT_SPAM;
		if(effect == EFFECT_CHAT_SPAM) return EFFECT_RANDOM_LOOT;
		return effect + 1;
	}

	private int nextConstrainedEffect() {
		if(this.remainingBlockEvents == 0 && this.remainingCobwebEvents == 0 && this.remainingInsultEvents == 0) {
			this.remainingBlockEvents = 4;
			this.remainingCobwebEvents = 2;
			this.remainingInsultEvents = 2;
		}
		int effect;
		do {
			int pick = this.random.nextInt(this.remainingBlockEvents + this.remainingCobwebEvents + this.remainingInsultEvents);
			if(pick < this.remainingBlockEvents) effect = EFFECT_BLOCK_EVENT;
			else if(pick < this.remainingBlockEvents + this.remainingCobwebEvents) effect = EFFECT_COBWEB;
			else effect = EFFECT_INSULTS;
		} while(effect == this.lastConstrainedEffect &&
			((effect == EFFECT_BLOCK_EVENT && this.remainingBlockEvents > 1) ||
			 (effect == EFFECT_COBWEB && this.remainingCobwebEvents > 1) ||
			 (effect == EFFECT_INSULTS && this.remainingInsultEvents > 1)));
		if(effect == EFFECT_BLOCK_EVENT) --this.remainingBlockEvents;
		else if(effect == EFFECT_COBWEB) --this.remainingCobwebEvents;
		else --this.remainingInsultEvents;
		this.lastConstrainedEffect = effect;
		return effect;
	}

	private String eventMessage(int effect) {
		if(effect != EFFECT_INSULTS) return "";
		String[] messages = {"You are not alone.", "Stop looking behind you.", "The world remembers your mistakes."};
		return "\u00a7c" + messages[this.random.nextInt(messages.length)];
	}

	private void applyPlayerEvent(int effect, WorldServer world) {
		if(effect == EFFECT_BLOCK_EVENT || effect == EFFECT_COBWEB) {
			java.util.List players = world == null ? this.server.configManager.playerEntities : world.playerEntities;
			for(int i = 0; i < players.size(); ++i) {
				EntityPlayerMP player = (EntityPlayerMP)players.get(i);
				int count = effect == EFFECT_BLOCK_EVENT ? 20 + this.random.nextInt(31) : 1;
				int placed = 0;
				for(int block = 0; block < count * 8 && placed < count; ++block) {
					int x = MathHelper.floor_double(player.posX) + this.random.nextInt(17) - 8;
					int y = effect == EFFECT_BLOCK_EVENT ? 80 + this.random.nextInt(21) : MathHelper.floor_double(player.posY) + this.random.nextInt(5) - 2;
					int z = MathHelper.floor_double(player.posZ) + this.random.nextInt(17) - 8;
					if(player.worldObj.getBlockId(x, y, z) != 0) continue;
					int blockId = effect == EFFECT_COBWEB ? Block.web.blockID : new int[]{Block.dirt.blockID, Block.cobblestone.blockID, Block.planks.blockID}[this.random.nextInt(3)];
					if(player.worldObj.setBlockAndMetadataWithNotify(x, y, z, blockId, 0)) {
						++placed;
						if(effect == EFFECT_COBWEB) break;
					}
				}
			}
			return;
		}
		if(effect != EFFECT_INVENTORY_SHUFFLE && effect != EFFECT_SIGN_SPAWN && effect != EFFECT_DROP_ACTIVE) return;
		java.util.List players = world == null ? this.server.configManager.playerEntities : world.playerEntities;
		for(int i = 0; i < players.size(); ++i) {
			EntityPlayerMP player = (EntityPlayerMP)players.get(i);
			if(effect == EFFECT_INVENTORY_SHUFFLE) {
				ItemStack[] allItems = new ItemStack[player.inventory.mainInventory.length + player.inventory.armorInventory.length];
				int index = 0;
				for(int slot = 0; slot < player.inventory.mainInventory.length; ++slot) allItems[index++] = player.inventory.mainInventory[slot];
				for(int slot = 0; slot < player.inventory.armorInventory.length; ++slot) allItems[index++] = player.inventory.armorInventory[slot];
				for(int slot = allItems.length - 1; slot > 0; --slot) {
					int swap = this.random.nextInt(slot + 1);
					ItemStack item = allItems[slot];
					allItems[slot] = allItems[swap];
					allItems[swap] = item;
				}
				index = 0;
				for(int slot = 0; slot < player.inventory.mainInventory.length; ++slot) player.inventory.mainInventory[slot] = allItems[index++];
				for(int slot = 0; slot < player.inventory.armorInventory.length; ++slot) player.inventory.armorInventory[slot] = allItems[index++];
				for(int slot = 0; slot < player.inventory.mainInventory.length; ++slot) {
					player.playerNetServerHandler.sendPacket(new Packet5PlayerInventory(player.entityId, slot, player.inventory.mainInventory[slot]));
				}
			} else if(effect == EFFECT_DROP_ACTIVE) {
				ItemStack active = player.inventory.decrStackSize(player.inventory.currentItem, player.inventory.getCurrentItem() == null ? 0 : player.inventory.getCurrentItem().stackSize);
				if(active != null) player.dropPlayerItem(active);
			} else {
				int x = MathHelper.floor_double(player.posX);
				int y = MathHelper.floor_double(player.posY);
				int z = MathHelper.floor_double(player.posZ);
				if(player.worldObj.getBlockId(x, y, z) == 0 && Block.signPost.canPlaceBlockAt(player.worldObj, x, y, z)) {
					player.worldObj.setBlockAndMetadataWithNotify(x, y, z, Block.signPost.blockID, 0);
					TileEntitySign sign = new TileEntitySign();
					sign.xCoord = x;
					sign.yCoord = y;
					sign.zCoord = z;
					sign.signText[0] = "no way";
					player.worldObj.setBlockTileEntity(x, y, z, sign);
				}
			}
		}
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
		return item.func_21132_c() ? this.random.nextInt(32) + 1 : 1;
	}

	private void giveItemToPlayers(int itemId, int itemCount, WorldServer world) {
		java.util.List players = world == null ? this.server.configManager.playerEntities : world.playerEntities;
		for(int i = 0; i < players.size(); ++i) {
			((EntityPlayerMP)players.get(i)).inventory.addItemStackToInventory(new ItemStack(itemId, itemCount, 0));
		}
	}
}
