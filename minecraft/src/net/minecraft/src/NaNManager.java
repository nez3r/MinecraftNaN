package net.minecraft.src;

import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Display;

/** Client-side, deliberately harmless approximations of the NaN events. */
public final class NaNManager {
	public static final int EFFECT_VOXEL_COLLAPSE = 2;
	public static final int EFFECT_FRAME_BLEED = 3;
	public static final int EFFECT_RED_TEXT = 4;
	public static final int EFFECT_INVENTORY_CORRUPTION = 5;
	public static final int EFFECT_RANDOM_LOOT = 6;
	public static final int EFFECT_RED_BARS = 7;
	public static final int EFFECT_BLOCK_EVENT = 8;
	public static final int EFFECT_COBWEB = 9;
	public static final int EFFECT_INSULTS = 10;
	public static final int EFFECT_INVENTORY_SHUFFLE = 11;
	public static final int EFFECT_MINIMAL_RENDER = 12;
	public static final int EFFECT_SIGN_SPAWN = 13;
	public static final int EFFECT_SCREEN_INVERSION = 14;
	public static final int EFFECT_DROP_ACTIVE = 15;
	public static final int EFFECT_WINDOW_SHAKE = 16;
	public static final int EFFECT_FAKE_ERROR = 17;
	public static final int EFFECT_CHAT_SPAM = 18;
	public static final int EFFECT_UI_JITTER = 19;
	public static final int EFFECT_RED_BUTTONS = 20;
	public static final int EFFECT_WINDOW_TITLE = 21;
	public static final int EFFECT_DEBUG_CORRUPTION = 22;
	public static final int EFFECT_MENU_SHAKE = 23;
	public static final int EFFECT_CAMERA_SHAKE = 24;
	public static final int EFFECT_TEXT_CORRUPTION = 25;
	public static final int EFFECT_STACK_CORRUPTION = 26;
	public static final int EFFECT_LOG_CORRUPTION = 27;
	public static final int EFFECT_TERRAIN_CORRUPTION = 28;
	private static final int HORROR_EVENT = 2;
	private static final int MIN_INTERVAL = 20 * 60 * 4;
	private static final int MAX_INTERVAL = 20 * 60 * 8;
	private static World lastWorld;
	private static int ticksUntilEvent;
	private static int activeEffect;
	private static int activeTicks;
	private static int nextEffect = EFFECT_RANDOM_LOOT;
	private static int lastSequence;
	private static int delayedInsultTicks;
	private static boolean fakeErrorInsults;
	private static int previousRenderDistance = -1;
	private static int previousWindowX;
	private static int previousWindowY;
	private static boolean windowPositionSaved;
	private static boolean permanentJitter;
	private static boolean permanentRedButtons;
	private static boolean permanentWindowTitle;
	private static boolean permanentDebugCorruption;
	private static boolean menuShakeArmed;
	private static int menuShakeTicks;
	private static int titleTicks;
	private static int redButtonTicks;
	private static int debugCorruptionTicks;
	private static String originalWindowTitle = "NaN";
	private static final Random RANDOM = new Random();
	private static int stackCorruptionLimit = 64;

	private NaNManager() {}

	public static void tick(Minecraft mc) {
		if(lastWorld != mc.theWorld) {
			if(lastWorld != null && mc.theWorld == null && menuShakeArmed) {
				menuShakeTicks = 20 * 5;
				saveWindowPosition();
			}
			lastWorld = mc.theWorld;
			if(mc.theWorld != null) {
				ticksUntilEvent = nextInterval();
				activeEffect = 0;
				activeTicks = 0;
				lastSequence = 0;
				delayedInsultTicks = 0;
			}
		}
		if(permanentWindowTitle) {
			++titleTicks;
			int length = Math.min(15, titleTicks / (20 * 15) + 1);
			String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
			StringBuffer title = new StringBuffer();
			for(int i = 0; i < length; ++i) title.append(symbols.charAt(RANDOM.nextInt(symbols.length())));
			Display.setTitle(title.toString());
		}
		if(permanentRedButtons) ++redButtonTicks;
		if(permanentDebugCorruption) ++debugCorruptionTicks;
		if(menuShakeTicks > 0) {
			--menuShakeTicks;
			if(!Display.isFullscreen()) {
				if(!windowPositionSaved) saveWindowPosition();
				Display.setLocation(previousWindowX + RANDOM.nextInt(41) - 20, previousWindowY + RANDOM.nextInt(41) - 20);
			}
			if(menuShakeTicks == 0 && windowPositionSaved) restoreWindowPosition();
		}
		if(mc.theWorld == null || mc.thePlayer == null) return;

		if(activeTicks > 0 && --activeTicks == 0) {
			int finishedEffect = activeEffect;
			activeEffect = 0;
			if(finishedEffect == EFFECT_MINIMAL_RENDER && previousRenderDistance >= 0) {
				mc.gameSettings.renderDistance = previousRenderDistance;
				previousRenderDistance = -1;
			}
			if(finishedEffect == EFFECT_WINDOW_SHAKE && windowPositionSaved) restoreWindowPosition();
			if(finishedEffect == EFFECT_RED_TEXT && fakeErrorInsults) fakeErrorInsults = false;
			if(finishedEffect == EFFECT_RED_BARS) mc.theWorld.setWorldTime(mc.theWorld.getWorldTime() - mc.theWorld.getWorldTime() % 24000L + 13000L);
			if(finishedEffect == EFFECT_STACK_CORRUPTION) stackCorruptionLimit = 64;
		}
		if(delayedInsultTicks > 0 && --delayedInsultTicks == 0) {
			activeEffect = EFFECT_RED_TEXT;
			activeTicks = 20 * 8;
			fakeErrorInsults = true;
		}
		if(activeEffect == EFFECT_INSULTS && activeTicks % 10 == 0) showLocalInsult(mc);
		if(activeEffect == EFFECT_CHAT_SPAM && activeTicks % 2 == 0) showRandomChatSymbols(mc);
		if(activeEffect == EFFECT_LOG_CORRUPTION) logRandomSymbols();
		if(activeEffect == EFFECT_WINDOW_SHAKE && !Display.isFullscreen()) {
			if(!windowPositionSaved) saveWindowPosition();
			Display.setLocation(previousWindowX + RANDOM.nextInt(17) - 8, previousWindowY + RANDOM.nextInt(17) - 8);
		}

		if(activeEffect == EFFECT_INVENTORY_CORRUPTION) return;
		if(mc.theWorld.multiplayerWorld) return;
		if(--ticksUntilEvent <= 0) {
			if(nextEffect == EFFECT_RANDOM_LOOT) startRandomLoot(mc);
			else startEffect(mc, nextEffect, effectDuration(nextEffect));
			nextEffect = nextEffectId(nextEffect);
			ticksUntilEvent = nextEffect == EFFECT_INVENTORY_CORRUPTION ? 20 * 60 * 2 : nextInterval();
		}
	}

	private static void saveWindowPosition() {
		previousWindowX = Display.getX();
		previousWindowY = Display.getY();
		windowPositionSaved = true;
	}

	private static void restoreWindowPosition() {
		Display.setLocation(previousWindowX, previousWindowY);
		windowPositionSaved = false;
	}

	private static int nextInterval() {
		return MIN_INTERVAL + RANDOM.nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
	}

	public static void handleEvent(Minecraft mc, Packet201HorrorEvent event) {
		if(event.eventId != HORROR_EVENT || event.sequence <= lastSequence) return;
		lastSequence = event.sequence;
		if(event.effectId == EFFECT_RANDOM_LOOT && !mc.theWorld.multiplayerWorld) giveItem(mc, event.itemId, event.itemCount);
		else {
			if(event.effectId == EFFECT_INVENTORY_SHUFFLE && !mc.theWorld.multiplayerWorld) shuffleInventory(mc);
			startEffect(mc, event.effectId, event.durationTicks);
			if(event.effectId == EFFECT_INSULTS && event.message != null && event.message.length() > 0) {
				mc.ingameGUI.addChatMessage(event.message);
			}
		}
	}

	private static void giveItem(Minecraft mc, int itemId, int itemCount) {
		if(itemId >= 0 && itemId < Item.itemsList.length && Item.itemsList[itemId] != null && itemCount > 0) {
			mc.thePlayer.inventory.addItemStackToInventory(new ItemStack(itemId, Math.min(itemCount, 32), 0));
		}
	}

	private static void startRandomLoot(Minecraft mc) {
		int itemId;
		do {
			itemId = RANDOM.nextInt(Item.itemsList.length);
		} while(Item.itemsList[itemId] == null);
		ItemStack item = new ItemStack(itemId, 1, 0);
		giveItem(mc, itemId, item.isStackable() ? RANDOM.nextInt(32) + 1 : 1);
	}

	private static void shuffleInventory(Minecraft mc) {
		ItemStack[] allItems = new ItemStack[mc.thePlayer.inventory.mainInventory.length + mc.thePlayer.inventory.armorInventory.length];
		int index = 0;
		for(int i = 0; i < mc.thePlayer.inventory.mainInventory.length; ++i) allItems[index++] = mc.thePlayer.inventory.mainInventory[i];
		for(int i = 0; i < mc.thePlayer.inventory.armorInventory.length; ++i) allItems[index++] = mc.thePlayer.inventory.armorInventory[i];
		for(int i = allItems.length - 1; i > 0; --i) {
			int swap = RANDOM.nextInt(i + 1);
			ItemStack item = allItems[i];
			allItems[i] = allItems[swap];
			allItems[swap] = item;
		}
		index = 0;
		for(int i = 0; i < mc.thePlayer.inventory.mainInventory.length; ++i) mc.thePlayer.inventory.mainInventory[i] = allItems[index++];
		for(int i = 0; i < mc.thePlayer.inventory.armorInventory.length; ++i) mc.thePlayer.inventory.armorInventory[i] = allItems[index++];
		mc.thePlayer.inventory.inventoryChanged = true;
	}

	private static void startEffect(Minecraft mc, int effectId, int durationTicks) {
		beginEffect(effectId, durationTicks);
		if(effectId == EFFECT_FAKE_ERROR && isActive(effectId)) {
			System.err.println("fullscreenEnabler failed to restore display mode.");
			mc.ingameGUI.addChatMessage("\u00a7cfullscreenEnabler failed to restore display mode.");
			delayedInsultTicks = 20 * 5;
		}
		if(effectId == EFFECT_MINIMAL_RENDER && previousRenderDistance < 0) {
			previousRenderDistance = mc.gameSettings.renderDistance;
			mc.gameSettings.renderDistance = 3;
		}
		if(effectId == EFFECT_WINDOW_SHAKE && !Display.isFullscreen()) {
			previousWindowX = Display.getX();
			previousWindowY = Display.getY();
			windowPositionSaved = true;
		}
		if(effectId == EFFECT_BLOCK_EVENT) spawnLocalBlocks(mc);
		if(effectId == EFFECT_COBWEB) spawnLocalCobweb(mc);
		if(isActive(effectId) && (effectId == EFFECT_VOXEL_COLLAPSE || effectId == EFFECT_FRAME_BLEED || effectId == EFFECT_RED_BARS || effectId == EFFECT_TERRAIN_CORRUPTION)) {
			mc.sndManager.playSoundFX(effectId == EFFECT_RED_BARS ? "glitch.glitch16" : effectId == EFFECT_TERRAIN_CORRUPTION ? "glitch.glitch14" : "glitch.glitch1", 1.0F, 1.0F);
		}
	}

	public static void beginEffect(int effectId, int durationTicks) {
		if(effectId < EFFECT_VOXEL_COLLAPSE || effectId > EFFECT_TERRAIN_CORRUPTION) return;
		if(effectId == EFFECT_UI_JITTER) permanentJitter = true;
		if(effectId == EFFECT_RED_BUTTONS) permanentRedButtons = true;
		if(effectId == EFFECT_WINDOW_TITLE) permanentWindowTitle = true;
		if(effectId == EFFECT_DEBUG_CORRUPTION) permanentDebugCorruption = true;
		if(effectId == EFFECT_MENU_SHAKE) menuShakeArmed = true;
		if(effectId == EFFECT_STACK_CORRUPTION) stackCorruptionLimit = 32 + RANDOM.nextInt(47);
		activeEffect = effectId;
		activeTicks = effectId == EFFECT_INVENTORY_CORRUPTION || durationTicks == 0 ? -1 : Math.max(1, durationTicks);
	}

	public static boolean isGuiJitterActive() { return permanentJitter; }
	public static void applyGuiJitter() {
		if(permanentJitter) GL11.glTranslatef(RANDOM.nextInt(7) - 3, RANDOM.nextInt(7) - 3, 0.0F);
	}
	public static boolean isRedButtonsActive() { return permanentRedButtons; }
	public static int getStackLimit(int normalLimit) { return isActive(EFFECT_STACK_CORRUPTION) ? stackCorruptionLimit : normalLimit; }
	public static int displayStackSize(int actual) { return isActive(EFFECT_STACK_CORRUPTION) ? stackCorruptionLimit : actual; }
	public static int redButtonLevel() { return Math.min(255, redButtonTicks * 255 / (20 * 30)); }
	public static boolean debugLineVisible(int line) {
		if(!permanentDebugCorruption) return true;
		return debugCorruptionTicks < (line + 1) * 100;
	}
	public static boolean debugCoordinatesVisible() { return !permanentDebugCorruption || debugCorruptionTicks < 300; }

	public static String debugCommand(Minecraft mc, String command) {
		String[] parts = command.trim().split(" ");
		if(parts.length == 0) return null;
		if("mst".equalsIgnoreCase(parts[0])) {
			return "Next NaN event: " + effectName(nextEffect) + " (random order: 4-8 minutes)";
		}
		if("next".equalsIgnoreCase(parts[0])) {
			if(nextEffect == EFFECT_RANDOM_LOOT) startRandomLoot(mc);
			else {
				if(nextEffect == EFFECT_INVENTORY_SHUFFLE) shuffleInventory(mc);
				if(nextEffect == EFFECT_DROP_ACTIVE && mc.thePlayer.inventory.getCurrentItem() != null) mc.thePlayer.dropPlayerItem(mc.thePlayer.inventory.decrStackSize(mc.thePlayer.inventory.currentItem, mc.thePlayer.inventory.getCurrentItem().stackSize));
				if(nextEffect == EFFECT_SIGN_SPAWN) spawnLocalSign(mc);
				startEffect(mc, nextEffect, effectDuration(nextEffect));
			}
			if(nextEffect == EFFECT_INSULTS) showLocalInsult(mc);
			String result = "Started NaN event: " + effectName(nextEffect);
			nextEffect = nextEffectId(nextEffect);
			return result;
		}
		if("event".equalsIgnoreCase(parts[0]) && parts.length > 1) {
			int effect = effectId(parts[1]);
			if(effect == 0) return "Unknown NaN event: " + parts[1];
			if(effect == EFFECT_RANDOM_LOOT) startRandomLoot(mc);
			else {
				if(effect == EFFECT_INVENTORY_SHUFFLE) shuffleInventory(mc);
				if(effect == EFFECT_DROP_ACTIVE && mc.thePlayer.inventory.getCurrentItem() != null) mc.thePlayer.dropPlayerItem(mc.thePlayer.inventory.decrStackSize(mc.thePlayer.inventory.currentItem, mc.thePlayer.inventory.getCurrentItem().stackSize));
				if(effect == EFFECT_SIGN_SPAWN) spawnLocalSign(mc);
				startEffect(mc, effect, effectDuration(effect));
			}
			if(effect == EFFECT_INSULTS) showLocalInsult(mc);
			return "Started NaN event: " + effectName(effect);
		}

		return "Usage: /mst, /event <name>, /next";
	}

	private static void showLocalInsult(Minecraft mc) {
		String[] messages = {"You are not alone.", "Stop looking behind you.", "The world remembers your mistakes."};
		mc.ingameGUI.addChatMessage("\u00a7c" + messages[RANDOM.nextInt(messages.length)]);
	}

	private static void showRandomChatSymbols(Minecraft mc) {
		String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}<>/?";
		StringBuilder message = new StringBuilder();
		for(int i = 0; i < 24; ++i) message.append(symbols.charAt(RANDOM.nextInt(symbols.length())));
		mc.ingameGUI.addChatMessage("\u00a7c" + message.toString());
	}

	private static void spawnLocalSign(Minecraft mc) {
		int x = MathHelper.floor_double(mc.thePlayer.posX);
		int y = MathHelper.floor_double(mc.thePlayer.posY);
		int z = MathHelper.floor_double(mc.thePlayer.posZ);
		if(mc.theWorld.getBlockId(x, y, z) == 0 && Block.signPost.canPlaceBlockAt(mc.theWorld, x, y, z)) {
			mc.theWorld.setBlockAndMetadataWithNotify(x, y, z, Block.signPost.blockID, 0);
			TileEntitySign sign = new TileEntitySign();
			sign.xCoord = x;
			sign.yCoord = y;
			sign.zCoord = z;
			sign.signText[0] = "no way";
			mc.theWorld.setBlockTileEntity(x, y, z, sign);
		}
	}

	private static void spawnLocalBlocks(Minecraft mc) {
			int target = 20 + RANDOM.nextInt(31);
			int placed = 0;
			for(int attempt = 0; attempt < target * 8 && placed < target; ++attempt) {
				int x = MathHelper.floor_double(mc.thePlayer.posX) + RANDOM.nextInt(17) - 8;
				int y = 80 + RANDOM.nextInt(21);
				int z = MathHelper.floor_double(mc.thePlayer.posZ) + RANDOM.nextInt(17) - 8;
				if(mc.theWorld.getBlockId(x, y, z) != 0) continue;
				int[] blocks = {Block.dirt.blockID, Block.cobblestone.blockID, Block.planks.blockID};
				if(mc.theWorld.setBlockAndMetadataWithNotify(x, y, z, blocks[RANDOM.nextInt(blocks.length)], 0)) ++placed;
			}
	}

	private static void spawnLocalCobweb(Minecraft mc) {
			for(int attempt = 0; attempt < 32; ++attempt) {
				int x = MathHelper.floor_double(mc.thePlayer.posX) + RANDOM.nextInt(9) - 4;
				int y = MathHelper.floor_double(mc.thePlayer.posY) + RANDOM.nextInt(5) - 2;
				int z = MathHelper.floor_double(mc.thePlayer.posZ) + RANDOM.nextInt(9) - 4;
				if(mc.theWorld.getBlockId(x, y, z) == 0 && mc.theWorld.setBlockAndMetadataWithNotify(x, y, z, Block.web.blockID, 0)) return;
		}
	}

	public static String effectName(int effectId) {
		switch(effectId) {
		case EFFECT_VOXEL_COLLAPSE: return "voxel";
		case EFFECT_FRAME_BLEED: return "bleed";
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
		case EFFECT_UI_JITTER: return "jitter";
		case EFFECT_RED_BUTTONS: return "redbuttons";
		case EFFECT_WINDOW_TITLE: return "title";
		case EFFECT_DEBUG_CORRUPTION: return "debug";
		case EFFECT_MENU_SHAKE: return "menushake";
		case EFFECT_CAMERA_SHAKE: return "camerashake";
		case EFFECT_TEXT_CORRUPTION: return "text";
		case EFFECT_STACK_CORRUPTION: return "stack";
		case EFFECT_LOG_CORRUPTION: return "log";
		case EFFECT_TERRAIN_CORRUPTION: return "terrain";
		default: return "unknown";
		}
	}

	private static int effectId(String name) {
		if("voxel".equalsIgnoreCase(name) || "collapse".equalsIgnoreCase(name)) return EFFECT_VOXEL_COLLAPSE;
		if("bleed".equalsIgnoreCase(name) || "wireframe".equalsIgnoreCase(name)) return EFFECT_FRAME_BLEED;
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
		if("jitter".equalsIgnoreCase(name) || "ui".equalsIgnoreCase(name)) return EFFECT_UI_JITTER;
		if("redbuttons".equalsIgnoreCase(name) || "buttons".equalsIgnoreCase(name)) return EFFECT_RED_BUTTONS;
		if("title".equalsIgnoreCase(name) || "windowtitle".equalsIgnoreCase(name)) return EFFECT_WINDOW_TITLE;
		if("debug".equalsIgnoreCase(name) || "f3".equalsIgnoreCase(name)) return EFFECT_DEBUG_CORRUPTION;
		if("menushake".equalsIgnoreCase(name) || "exitshake".equalsIgnoreCase(name)) return EFFECT_MENU_SHAKE;
		if("camerashake".equalsIgnoreCase(name) || "camera".equalsIgnoreCase(name)) return EFFECT_CAMERA_SHAKE;
		if("text".equalsIgnoreCase(name) || "textcorruption".equalsIgnoreCase(name) || "symbols".equalsIgnoreCase(name)) return EFFECT_TEXT_CORRUPTION;
		if("stack".equalsIgnoreCase(name) || "stackcorruption".equalsIgnoreCase(name)) return EFFECT_STACK_CORRUPTION;
		if("log".equalsIgnoreCase(name) || "logcorruption".equalsIgnoreCase(name)) return EFFECT_LOG_CORRUPTION;
		if("terrain".equalsIgnoreCase(name) || "texture".equalsIgnoreCase(name)) return EFFECT_TERRAIN_CORRUPTION;
		return 0;
	}

	private static int effectDuration(int effectId) {
		if(effectId == EFFECT_FRAME_BLEED) return 20 * 7;
		if(effectId == EFFECT_RED_TEXT) return 20 * 15;
		if(effectId == EFFECT_INVENTORY_CORRUPTION || effectId == EFFECT_RANDOM_LOOT) return 0;
		if(effectId == EFFECT_RED_BARS) return 246;
		if(effectId == EFFECT_BLOCK_EVENT) return 20 * 5;
		if(effectId == EFFECT_COBWEB) return 20 * 8;
		if(effectId == EFFECT_INSULTS) return 20 * 4;
		if(effectId == EFFECT_INVENTORY_SHUFFLE || effectId == EFFECT_SIGN_SPAWN || effectId == EFFECT_DROP_ACTIVE) return 1;
		if(effectId == EFFECT_MINIMAL_RENDER) return 20 * 20;
		if(effectId == EFFECT_SCREEN_INVERSION) return 20 * 8;
		if(effectId == EFFECT_WINDOW_SHAKE) return 20 * 10;
		if(effectId == EFFECT_CAMERA_SHAKE) return 20 * 10;
		if(effectId == EFFECT_TEXT_CORRUPTION) return 20 * 30;
		if(effectId == EFFECT_STACK_CORRUPTION) return 20 * 60 * 5;
		if(effectId == EFFECT_LOG_CORRUPTION) return 20 * 15;
		if(effectId == EFFECT_TERRAIN_CORRUPTION) return 20 * 15;
		if(effectId == EFFECT_FAKE_ERROR) return 20 * 12;
		if(effectId == EFFECT_CHAT_SPAM) return 20 * 10;
		if(effectId == EFFECT_UI_JITTER || effectId == EFFECT_RED_BUTTONS || effectId == EFFECT_WINDOW_TITLE ||
			effectId == EFFECT_DEBUG_CORRUPTION || effectId == EFFECT_MENU_SHAKE) return 0;
		return 20 * 5;
	}

	private static int nextEffectId(int effectId) {
		if(effectId == EFFECT_RANDOM_LOOT) return EFFECT_VOXEL_COLLAPSE;
		if(effectId == EFFECT_RED_TEXT) return EFFECT_RED_BARS;
		if(effectId == EFFECT_RED_BARS) return EFFECT_UI_JITTER;
		if(effectId == EFFECT_UI_JITTER) return EFFECT_RED_BUTTONS;
		if(effectId == EFFECT_RED_BUTTONS) return EFFECT_WINDOW_TITLE;
		if(effectId == EFFECT_WINDOW_TITLE) return EFFECT_DEBUG_CORRUPTION;
		if(effectId == EFFECT_DEBUG_CORRUPTION) return EFFECT_MENU_SHAKE;
		if(effectId == EFFECT_MENU_SHAKE) return EFFECT_CAMERA_SHAKE;
		if(effectId == EFFECT_CAMERA_SHAKE) return EFFECT_TEXT_CORRUPTION;
		if(effectId == EFFECT_TEXT_CORRUPTION) return EFFECT_STACK_CORRUPTION;
		if(effectId == EFFECT_STACK_CORRUPTION) return EFFECT_LOG_CORRUPTION;
		if(effectId == EFFECT_LOG_CORRUPTION) return EFFECT_TERRAIN_CORRUPTION;
		if(effectId == EFFECT_TERRAIN_CORRUPTION) return EFFECT_INVENTORY_CORRUPTION;
		if(effectId == EFFECT_INVENTORY_CORRUPTION) return EFFECT_BLOCK_EVENT;
		if(effectId == EFFECT_COBWEB) return EFFECT_INSULTS;
		if(effectId == EFFECT_INSULTS) return EFFECT_INVENTORY_SHUFFLE;
		if(effectId == EFFECT_FAKE_ERROR) return EFFECT_CHAT_SPAM;
		if(effectId == EFFECT_CHAT_SPAM) return EFFECT_UI_JITTER;
		return effectId + 1;
	}

	public static boolean isActive(int effectId) {
		return activeEffect == effectId && (activeTicks > 0 || effectId == EFFECT_INVENTORY_CORRUPTION);
	}

	public static String randomizeItemName(String name) {
		String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}<>/?";
		StringBuilder result = new StringBuilder(name.length());
		for(int i = 0; i < name.length(); ++i) {
			char character = name.charAt(i);
			result.append(Character.isWhitespace(character) ? character : symbols.charAt(RANDOM.nextInt(symbols.length())));
		}
		return result.toString();
	}

	public static String randomizeText(String text) {
			if(!isActive(EFFECT_TEXT_CORRUPTION) || text == null) return text;
			String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}<>/?";
			StringBuffer result = new StringBuffer(text.length());
			for(int i = 0; i < text.length(); ++i) {
				char c = text.charAt(i);
				if(c == 167 && i + 1 < text.length()) {
					result.append(c).append(text.charAt(++i));
				} else {
					result.append(c == 167 || Character.isWhitespace(c) ? c : symbols.charAt(RANDOM.nextInt(symbols.length())));
				}
			}
			return result.toString();
		}

	private static void logRandomSymbols() {
			String symbols = "!@#$%^&*()_+-=[]{}<>/?ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
			StringBuffer result = new StringBuffer(32);
			for(int i = 0; i < 32; ++i) result.append(symbols.charAt(RANDOM.nextInt(symbols.length())));
			System.out.println(result.toString());
	}

	public static void applyProjectionEffect() {
		if(isActive(EFFECT_SCREEN_INVERSION)) {
			GL11.glScalef(-1.0F, -1.0F, 1.0F);
		}
		if(isActive(EFFECT_WINDOW_SHAKE)) {
			float shake = (float)Math.sin((double)activeTicks * 1.7D) * 0.025F;
			GL11.glTranslatef(shake, -shake, 0.0F);
		}
		if(isActive(EFFECT_CAMERA_SHAKE)) {
			GL11.glTranslatef((RANDOM.nextFloat() - 0.5F) * 0.18F, (RANDOM.nextFloat() - 0.5F) * 0.18F, 0.0F);
			GL11.glRotatef((RANDOM.nextFloat() - 0.5F) * 2.0F, 0.0F, 0.0F, 1.0F);
		}
		if(isActive(EFFECT_VOXEL_COLLAPSE)) {
			float phase = (activeTicks % 8) / 8.0F - 0.5F;
			float pulse = 1.0F + Math.abs(phase) * 0.18F;
			GL11.glTranslatef(phase * 0.16F, phase * 0.05F, 0.0F);
			GL11.glRotatef(phase * 3.0F, 0.0F, 0.0F, 1.0F);
			GL11.glScalef(pulse, 1.0F + Math.abs(phase) * 0.08F, 1.0F);
		}
	}

	public static boolean hideHud() {
		return activeEffect == EFFECT_VOXEL_COLLAPSE || activeEffect == EFFECT_FRAME_BLEED || activeEffect == EFFECT_RED_TEXT ||
			activeEffect == EFFECT_RED_BARS || activeEffect == EFFECT_MINIMAL_RENDER || activeEffect == EFFECT_FAKE_ERROR ||
			activeEffect == EFFECT_TERRAIN_CORRUPTION;
	}

	public static void renderEffectOverlay(Minecraft mc, int width, int height) {
		if(!hideHud()) return;
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)width, (double)height, 0.0D, -100.0D, 100.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if(activeEffect == EFFECT_RED_TEXT) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			if(fakeErrorInsults) {
				String[] insults = {"You are weak.", "Nobody will find you.", "Stop pretending.", "You should have left.", "This world hates you.",
					"You are being watched.", "There is no escape.", "You made a mistake.", "Do not turn around.", "You are already lost."};
				for(int i = 0; i < insults.length; ++i) {
					int x = RANDOM.nextInt(Math.max(1, width - 80));
					int y = RANDOM.nextInt(Math.max(1, height - 10));
					mc.fontRenderer.drawString(insults[i], x, y, 16711680);
				}
			} else {
				int lineHeight = 9;
				int lineCount = height / lineHeight + 2;
				String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}<>/?";
				for(int line = 0; line < lineCount; ++line) {
					StringBuilder text = new StringBuilder();
					for(int i = 0; i < width / 4 + 48; ++i) {
						text.append(symbols.charAt(RANDOM.nextInt(symbols.length())));
					}
					int x = (activeTicks * (3 + line % 4) + line * 37) % 24 - 12;
					int y = line * lineHeight - 2;
					GL11.glColor4f(1.0F, 0.02F, 0.02F, 1.0F);
					mc.fontRenderer.drawString(text.toString(), x, y, 16711680);
				}
			}
		} else if(activeEffect == EFFECT_TERRAIN_CORRUPTION) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/terrain.png"));
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			for(int copy = 0; copy < 50; ++copy) {
				int x = RANDOM.nextInt(Math.max(1, width));
				int y = RANDOM.nextInt(Math.max(1, height));
				int size = 8 + RANDOM.nextInt(48);
				float u = RANDOM.nextFloat();
				float v = RANDOM.nextFloat();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
				GL11.glTexCoord2f(u, v); GL11.glVertex2f(x, y);
				GL11.glTexCoord2f(u + 0.08F, v); GL11.glVertex2f(x + size, y);
				GL11.glTexCoord2f(u + 0.08F, v + 0.08F); GL11.glVertex2f(x + size, y + size);
				GL11.glTexCoord2f(u, v + 0.08F); GL11.glVertex2f(x, y + size);
				GL11.glEnd();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.45F);
				drawBand(x - 30, y, size + 60, 1);
			}
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else if(activeEffect == EFFECT_VOXEL_COLLAPSE) {
			float pulse = 0.18F + (activeTicks % 6) * 0.025F;
			GL11.glColor4f(0.35F, 0.0F, 0.65F, pulse);
			drawBand(0, 0, width, height);
			GL11.glColor4f(1.0F, 0.0F, 0.05F, 0.22F);
			drawBand(0, (activeTicks * 13) % height, width, height / 7 + 2);
			GL11.glColor4f(0.1F, 0.0F, 0.25F, 0.3F);
			for(int y = 0; y < height; y += 12) drawBand(0, y, width, 3);
		} else if(activeEffect == EFFECT_RED_BARS) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			int barCount = 20 + RANDOM.nextInt(31);
			for(int i = 0; i < barCount; ++i) {
				int y = RANDOM.nextInt(Math.max(1, height));
				int barHeight = 1 + RANDOM.nextInt(3);
				int x = -RANDOM.nextInt(Math.max(1, width / 3));
				int barWidth = width + RANDOM.nextInt(Math.max(1, width / 2));
				float brightness = 0.65F + RANDOM.nextFloat() * 0.35F;
				GL11.glColor3f(brightness, 0.0F, 0.0F);
				drawBand(x, y, barWidth, barHeight);
			}
		} else if(activeEffect == EFFECT_FAKE_ERROR) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			mc.fontRenderer.drawString(" ", width / 2 - 72, height / 2 - 12, 16711680);
			mc.fontRenderer.drawString(" ", width / 2 - 105, height / 2 + 2, 16711680);
		} else {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.28F);
			for(int y = 0; y < height; y += 14) {
				int offset = ((activeTicks + y) % 3 - 1) * (width / 12);
				drawBand(offset, y, width, 4);
			}
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.12F);
			drawBand(0, (activeTicks * 17) % height, width, height / 10 + 1);
		}
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
	}

	private static void drawBand(int x, int y, int bandWidth, int bandHeight) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(x, y, 0.0F);
		GL11.glVertex3f(x + bandWidth, y, 0.0F);
		GL11.glVertex3f(x + bandWidth, y + bandHeight, 0.0F);
		GL11.glVertex3f(x, y + bandHeight, 0.0F);
		GL11.glEnd();
	}

}
