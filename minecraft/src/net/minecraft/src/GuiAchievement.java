package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiAchievement extends Gui {
	private Minecraft field_25082_a;
	private int field_25081_b;
	private int field_25086_c;
	private String field_25085_d;
	private String field_25084_e;
	private Achievement field_27105_f;
	private long field_25083_f;
	private RenderItem field_27104_h;
	private boolean field_27103_i;

	public GuiAchievement(Minecraft var1) {
		this.field_25082_a = var1;
		this.field_27104_h = new RenderItem();
	}

	public void func_27102_a(Achievement var1) {
		this.field_25085_d = StatCollector.translateToLocal("achievement.get");
		this.field_25084_e = var1.statName;
		this.field_25083_f = System.currentTimeMillis();
		this.field_27105_f = var1;
		this.field_27103_i = false;
	}

	public void func_27101_b(Achievement var1) {
		this.field_25085_d = var1.statName;
		this.field_25084_e = var1.func_27090_e();
		this.field_25083_f = System.currentTimeMillis() - 2500L;
		this.field_27105_f = var1;
		this.field_27103_i = true;
	}

	private void func_25079_b() {
		GL11.glViewport(0, 0, this.field_25082_a.displayWidth, this.field_25082_a.displayHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		this.field_25081_b = this.field_25082_a.displayWidth;
		this.field_25086_c = this.field_25082_a.displayHeight;
		ScaledResolution var1 = new ScaledResolution(this.field_25082_a.gameSettings, this.field_25082_a.displayWidth, this.field_25082_a.displayHeight);
		this.field_25081_b = var1.getScaledWidth();
		this.field_25086_c = var1.getScaledHeight();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)this.field_25081_b, (double)this.field_25086_c, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}

	public void func_25080_a() {
		if(this.field_27105_f != null && this.field_25083_f != 0L) {
			double var1 = (double)(System.currentTimeMillis() - this.field_25083_f) / 3000.0D;
			if(this.field_27103_i || this.field_27103_i || var1 >= 0.0D && var1 <= 1.0D) {
				this.func_25079_b();
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glDepthMask(false);
				double var3 = var1 * 2.0D;
				if(var3 > 1.0D) {
					var3 = 2.0D - var3;
				}

				var3 *= 4.0D;
				var3 = 1.0D - var3;
				if(var3 < 0.0D) {
					var3 = 0.0D;
				}

				var3 *= var3;
				var3 *= var3;
				int var5 = this.field_25081_b - 160;
				int var6 = 0 - (int)(var3 * 36.0D);
				int var7 = this.field_25082_a.renderEngine.getTexture("/achievement/bg.png");
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, var7);
				GL11.glDisable(GL11.GL_LIGHTING);
				this.drawTexturedModalRect(var5, var6, 96, 202, 160, 32);
				if(this.field_27103_i) {
					this.field_25082_a.fontRenderer.func_27278_a(this.field_25084_e, var5 + 30, var6 + 7, 120, -1);
				} else {
					this.field_25082_a.fontRenderer.drawString(this.field_25085_d, var5 + 30, var6 + 7, -256);
					this.field_25082_a.fontRenderer.drawString(this.field_25084_e, var5 + 30, var6 + 18, -1);
				}

				GL11.glPushMatrix();
				GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
				RenderHelper.enableStandardItemLighting();
				GL11.glPopMatrix();
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glEnable(GL11.GL_COLOR_MATERIAL);
				GL11.glEnable(GL11.GL_LIGHTING);
				this.field_27104_h.renderItemIntoGUI(this.field_25082_a.fontRenderer, this.field_25082_a.renderEngine, this.field_27105_f.field_27097_d, var5 + 8, var6 + 8);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			} else {
				this.field_25083_f = 0L;
			}
		}
	}
}
