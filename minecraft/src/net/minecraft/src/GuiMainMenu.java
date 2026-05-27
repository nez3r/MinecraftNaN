package net.minecraft.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import org.lwjgl.opengl.GL11;

public class GuiMainMenu extends GuiScreen {
	private static final Random rand = new Random();
	private float updateCounter = 0.0F;
	private String splashText = "missingno";
	private GuiButton field_25096_l;
	private int glitchTimer = 0;
	private boolean titleGlitch = false;

	private static final String[] NAN_SPLASHES = new String[]{
		"NaN NaN NaN NaN NaN",
		"java.lang.NullPointerException",
		"ERROR: WORLD_SEED == NaN",
		"He is watching",
		"Don't look behind you",
		"FATAL: memory corrupted",
		"0x00000000",
		"Wake up",
		"This is not real",
		"undefined",
		"null",
		"[Ljava.lang.Object;@NaN",
		"Connection lost: NaN",
		"missingno",
		"Error 404: Reality not found"
	};

	public GuiMainMenu() {
		this.splashText = NAN_SPLASHES[rand.nextInt(NAN_SPLASHES.length)];
	}

	public void updateScreen() {
		++this.updateCounter;
		++this.glitchTimer;
		if(this.glitchTimer % 60 == 0) {
			this.titleGlitch = rand.nextInt(4) == 0;
		}
		if(this.glitchTimer % 200 == 0) {
			this.splashText = NAN_SPLASHES[rand.nextInt(NAN_SPLASHES.length)];
		}
	}

	protected void keyTyped(char var1, int var2) {
	}

	public void initGui() {
		Calendar var1 = Calendar.getInstance();
		var1.setTime(new Date());
		if(var1.get(2) + 1 == 11 && var1.get(5) == 9) {
			this.splashText = "Happy birthday, ez!";
		} else if(var1.get(2) + 1 == 6 && var1.get(5) == 1) {
			this.splashText = "Happy birthday, Notch!";
		} else if(var1.get(2) + 1 == 12 && var1.get(5) == 24) {
			this.splashText = "Merry X-mas!";
		} else if(var1.get(2) + 1 == 1 && var1.get(5) == 1) {
			this.splashText = "Happy new year!";
		}

		StringTranslate var2 = StringTranslate.getInstance();
		int var4 = this.height / 4 + 48;
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, var4, var2.translateKey("menu.singleplayer")));
		this.controlList.add(this.field_25096_l = new GuiButton(2, this.width / 2 - 100, var4 + 24, var2.translateKey("menu.multiplayer")));
		this.controlList.add(new GuiButton(3, this.width / 2 - 100, var4 + 48, var2.translateKey("menu.mods")));
		if(this.mc.hideQuitButton) {
			this.controlList.add(new GuiButton(0, this.width / 2 - 100, var4 + 72, var2.translateKey("menu.options")));
		} else {
			this.controlList.add(new GuiButton(0, this.width / 2 - 100, var4 + 72 + 12, 98, 20, var2.translateKey("menu.options")));
			this.controlList.add(new GuiButton(4, this.width / 2 + 2, var4 + 72 + 12, 98, 20, var2.translateKey("menu.quit")));
		}

		if(this.mc.session == null) {
			this.field_25096_l.enabled = false;
		}

	}

	protected void actionPerformed(GuiButton var1) {
		if(var1.id == 0) {
			this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
		}

		if(var1.id == 1) {
			this.mc.displayGuiScreen(new GuiSelectWorld(this));
		}

		if(var1.id == 2) {
			this.mc.displayGuiScreen(new GuiMultiplayer(this));
		}

		if(var1.id == 3) {
			this.mc.displayGuiScreen(new GuiTexturePacks(this));
		}

		if(var1.id == 4) {
			this.mc.shutdown();
		}

	}

	public void drawScreen(int var1, int var2, float var3) {
		this.drawDefaultBackground();
		Tessellator var4 = Tessellator.instance;
		short var5 = 274;
		int var6 = this.width / 2 - var5 / 2;
		byte var7 = 30;

		// Glitchy title rendering
		if(this.titleGlitch) {
			int offsetX = rand.nextInt(5) - 2;
			int offsetY = rand.nextInt(3) - 1;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/title/mclogo.png"));
			GL11.glColor4f(0.8F, 0.2F, 0.2F, 0.9F);
			this.drawTexturedModalRect(var6 + offsetX, var7 + offsetY, 0, 0, 155, 44);
			this.drawTexturedModalRect(var6 + 155 + offsetX, var7 + offsetY, 0, 45, 155, 44);
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/title/mclogo.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(var6 + 0, var7 + 0, 0, 0, 155, 44);
		this.drawTexturedModalRect(var6 + 155, var7 + 0, 0, 45, 155, 44);
		var4.setColorOpaque_I(16777215);
		GL11.glPushMatrix();
		GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
		GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		float var8 = 1.8F - MathHelper.abs(MathHelper.sin((float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
		var8 = var8 * 100.0F / (float)(this.fontRenderer.getStringWidth(this.splashText) + 32);
		GL11.glScalef(var8, var8, var8);
		int splashColor = this.titleGlitch ? 0xFF3333 : 16776960;
		this.drawCenteredString(this.fontRenderer, this.splashText, 0, -8, splashColor);
		GL11.glPopMatrix();

		String titleStr = this.titleGlitch ? "Minecraft N§ka§rN" : "Minecraft NaN";
		this.drawString(this.fontRenderer, titleStr, 2, 2, 5263440);
		String var9 = "Copyright Mojang AB. Do not distribute.";
		this.drawString(this.fontRenderer, var9, this.width - this.fontRenderer.getStringWidth(var9) - 2, this.height - 10, 16777215);

		// Random glitch text at bottom
		if(this.glitchTimer % 80 < 10) {
			String glitchMsg = "§4YOU ARE DIE!";
			this.drawString(this.fontRenderer, glitchMsg, 2, this.height - 20, 0x880000);
		}

		super.drawScreen(var1, var2, var3);
	}
}
