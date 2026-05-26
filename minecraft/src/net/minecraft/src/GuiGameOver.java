package net.minecraft.src;

import java.util.Random;
import org.lwjgl.opengl.GL11;

public class GuiGameOver extends GuiScreen {
	private Random rand = new Random();
	private int retryClickCount = 0;
	private int tickCounter = 0;
	private boolean glitchFlash = false;

	public void initGui() {
		this.controlList.clear();
		this.retryClickCount = 0;
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, "Retry?"));
		this.controlList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 120, "Title menu"));
		if(this.mc.session == null) {
			((GuiButton)this.controlList.get(1)).enabled = false;
		}
	}

	public void updateScreen() {
		++this.tickCounter;
		if(this.tickCounter % 15 == 0) {
			this.glitchFlash = this.rand.nextInt(3) == 0;
		}

		GuiButton retryBtn = (GuiButton)this.controlList.get(0);
		if(this.tickCounter % 20 == 0 && this.retryClickCount < 3) {
			int jitterX = this.rand.nextInt(11) - 5;
			int jitterY = this.rand.nextInt(7) - 3;
			retryBtn.xPosition = this.width / 2 - 100 + jitterX;
			retryBtn.yPosition = this.height / 4 + 96 + jitterY;
		}
	}

	protected void keyTyped(char var1, int var2) {
	}

	protected void actionPerformed(GuiButton var1) {
		if(var1.id == 0) {
		}

		if(var1.id == 1) {
			++this.retryClickCount;
			if(this.retryClickCount >= 3) {
				this.mc.thePlayer.respawnPlayer();
				this.mc.displayGuiScreen((GuiScreen)null);
			}
		}

		if(var1.id == 2) {
			this.mc.changeWorld1((World)null);
			this.mc.displayGuiScreen(new GuiMainMenu());
		}
	}

	public void drawScreen(int var1, int var2, float var3) {
		this.drawGradientRect(0, 0, this.width, this.height, 0xCC000000, 0xCC200000);

		int errorColor = 0xCC3333;
		int dimColor = 0x888888;

		GL11.glPushMatrix();
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		this.drawCenteredString(this.fontRenderer, "java.lang.StackOverflowError", this.width / 2 * 2 / 3, 20, errorColor);
		GL11.glPopMatrix();

		int yStart = this.height / 4 - 10;
		this.drawCenteredString(this.fontRenderer, "at net.minecraft.src.World.tick(World.java:NaN)", this.width / 2, yStart, dimColor);
		this.drawCenteredString(this.fontRenderer, "at net.minecraft.src.Entity.onUpdate(Entity.java:NaN)", this.width / 2, yStart + 12, dimColor);
		this.drawCenteredString(this.fontRenderer, "at net.minecraft.client.Minecraft.run(Minecraft.java:NaN)", this.width / 2, yStart + 24, dimColor);

		if(this.glitchFlash) {
			this.drawCenteredString(this.fontRenderer, "at java.lang.Thread.run(Thread.java:" + this.rand.nextInt(999) + ")", this.width / 2, yStart + 36, 0xFF0000);
		} else {
			this.drawCenteredString(this.fontRenderer, "at java.lang.Thread.run(Thread.java:NaN)", this.width / 2, yStart + 36, dimColor);
		}

		if(this.retryClickCount > 0 && this.retryClickCount < 3) {
			String msg = this.retryClickCount == 1 ? "§cRetry failed. Try again." : "§4Connection unstable...";
			this.drawCenteredString(this.fontRenderer, msg, this.width / 2, this.height / 4 + 80, 0xFF4444);
		}

		this.drawCenteredString(this.fontRenderer, "Score: §e" + this.mc.thePlayer.getScore(), this.width / 2, this.height / 4 + 60, 16777215);

		super.drawScreen(var1, var2, var3);
	}

	public boolean doesGuiPauseGame() {
		return false;
	}
}
