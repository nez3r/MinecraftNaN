package net.minecraft.src;

import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Client-side, deliberately harmless approximations of the NaN events. */
public final class NaNManager {
	public static final int EFFECT_VOXEL_COLLAPSE = 2;
	public static final int EFFECT_FRAME_BLEED = 3;
	public static final int EFFECT_RED_TEXT = 4;
	public static final int EFFECT_INVENTORY_CORRUPTION = 5;
	private static final int HORROR_EVENT = 2;
	private static final int MIN_INTERVAL = 20 * 60 * 4;
	private static final int MAX_INTERVAL = 20 * 60 * 8;
	private static World lastWorld;
	private static int ticksUntilEvent;
	private static int activeEffect;
	private static int activeTicks;
	private static int nextEffect = EFFECT_VOXEL_COLLAPSE;
	private static final Random RANDOM = new Random();

	private NaNManager() {}

	public static void tick(Minecraft mc) {
		if(mc.theWorld == null || mc.thePlayer == null) return;
		if(lastWorld != mc.theWorld) {
			lastWorld = mc.theWorld;
			ticksUntilEvent = nextInterval();
			activeEffect = 0;
			activeTicks = 0;
		}
		if(activeTicks > 0 && --activeTicks == 0) activeEffect = 0;
		if(activeEffect == EFFECT_INVENTORY_CORRUPTION) return;
		if(mc.theWorld.multiplayerWorld) return;
		if(--ticksUntilEvent <= 0) {
			startEffect(mc, nextEffect, effectDuration(nextEffect));
			nextEffect = nextEffect == EFFECT_INVENTORY_CORRUPTION ? EFFECT_INVENTORY_CORRUPTION : nextEffect + 1;
			ticksUntilEvent = nextEffect == EFFECT_INVENTORY_CORRUPTION ? 20 * 60 * 2 : nextInterval();
		}
	}

	private static int nextInterval() {
		return MIN_INTERVAL + RANDOM.nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
	}

	public static void handleEvent(Minecraft mc, int eventId, int effectId, int durationTicks) {
		if(eventId == HORROR_EVENT) {
			startEffect(mc, effectId, durationTicks);
		}
	}

	private static void startEffect(Minecraft mc, int effectId, int durationTicks) {
		beginEffect(effectId, durationTicks);
		if(isActive(effectId) && effectId != EFFECT_RED_TEXT) {
			mc.sndManager.playSoundFX("glitch.glitch1", 1.0F, 1.0F);
		}
	}

	public static void beginEffect(int effectId, int durationTicks) {
		if(effectId < EFFECT_VOXEL_COLLAPSE || effectId > EFFECT_INVENTORY_CORRUPTION) return;
		activeEffect = effectId;
		activeTicks = effectId == EFFECT_INVENTORY_CORRUPTION ? -1 : Math.max(1, Math.min(durationTicks, 20 * 15));
	}

	public static String debugCommand(Minecraft mc, String command) {
		String[] parts = command.trim().split(" ");
		if(parts.length == 0) return null;
		if("mst".equalsIgnoreCase(parts[0])) {
			return "Next NaN event: " + effectName(nextEffect) + " (random order: 4-8 minutes)";
		}
		if("next".equalsIgnoreCase(parts[0])) {
			startEffect(mc, nextEffect, effectDuration(nextEffect));
			String result = "Started NaN event: " + effectName(nextEffect);
			nextEffect = nextEffect == EFFECT_INVENTORY_CORRUPTION ? EFFECT_INVENTORY_CORRUPTION : nextEffect + 1;
			return result;
		}
		if("event".equalsIgnoreCase(parts[0]) && parts.length > 1) {
			int effect = effectId(parts[1]);
			if(effect == 0) return "Unknown NaN event: " + parts[1];
			startEffect(mc, effect, effectDuration(effect));
			return "Started NaN event: " + effectName(effect);
		}
		return "Usage: /mst, /event <name>, /next";
	}

	public static String effectName(int effectId) {
		switch(effectId) {
		case EFFECT_VOXEL_COLLAPSE: return "voxel";
		case EFFECT_FRAME_BLEED: return "bleed";
		case EFFECT_RED_TEXT: return "redtext";
		case EFFECT_INVENTORY_CORRUPTION: return "inventory";
		default: return "unknown";
		}
	}

	private static int effectId(String name) {
		if("voxel".equalsIgnoreCase(name) || "collapse".equalsIgnoreCase(name)) return EFFECT_VOXEL_COLLAPSE;
		if("bleed".equalsIgnoreCase(name) || "wireframe".equalsIgnoreCase(name)) return EFFECT_FRAME_BLEED;
		if("redtext".equalsIgnoreCase(name) || "red".equalsIgnoreCase(name)) return EFFECT_RED_TEXT;
		if("inventory".equalsIgnoreCase(name) || "items".equalsIgnoreCase(name)) return EFFECT_INVENTORY_CORRUPTION;
		return 0;
	}

	private static int effectDuration(int effectId) {
		if(effectId == EFFECT_FRAME_BLEED) return 20 * 7;
		if(effectId == EFFECT_RED_TEXT) return 20 * 15;
		if(effectId == EFFECT_INVENTORY_CORRUPTION) return 0;
		return 20 * 5;
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

	public static void applyProjectionEffect() {
		if(isActive(EFFECT_VOXEL_COLLAPSE)) {
			float phase = (activeTicks % 8) / 8.0F - 0.5F;
			float pulse = 1.0F + Math.abs(phase) * 0.18F;
			GL11.glTranslatef(phase * 0.16F, phase * 0.05F, 0.0F);
			GL11.glRotatef(phase * 3.0F, 0.0F, 0.0F, 1.0F);
			GL11.glScalef(pulse, 1.0F + Math.abs(phase) * 0.08F, 1.0F);
		}
	}

	public static boolean hideHud() {
		return activeEffect == EFFECT_VOXEL_COLLAPSE || activeEffect == EFFECT_FRAME_BLEED || activeEffect == EFFECT_RED_TEXT;
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
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if(activeEffect == EFFECT_RED_TEXT) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
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
		} else if(activeEffect == EFFECT_VOXEL_COLLAPSE) {
			float pulse = 0.18F + (activeTicks % 6) * 0.025F;
			GL11.glColor4f(0.35F, 0.0F, 0.65F, pulse);
			drawBand(0, 0, width, height);
			GL11.glColor4f(1.0F, 0.0F, 0.05F, 0.22F);
			drawBand(0, (activeTicks * 13) % height, width, height / 7 + 2);
			GL11.glColor4f(0.1F, 0.0F, 0.25F, 0.3F);
			for(int y = 0; y < height; y += 12) drawBand(0, y, width, 3);
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
		GL11.glVertex3f(x, y, -90.0F);
		GL11.glVertex3f(x + bandWidth, y, -90.0F);
		GL11.glVertex3f(x + bandWidth, y + bandHeight, -90.0F);
		GL11.glVertex3f(x, y + bandHeight, -90.0F);
		GL11.glEnd();
	}

}
