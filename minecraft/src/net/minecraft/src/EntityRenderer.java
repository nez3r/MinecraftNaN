package net.minecraft.src;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.NVFogDistance;
import org.lwjgl.util.glu.GLU;

public class EntityRenderer {
        private Minecraft mc;
        private float farPlaneDistance = 0.0F;
        public ItemRenderer itemRenderer;
        private int field_1386_j;
        private Entity field_1385_k = null;
        private MouseFilter mouseFilterXAxis = new MouseFilter();
        private MouseFilter mouseFilterYAxis = new MouseFilter();
        private MouseFilter field_22233_n = new MouseFilter();
        private MouseFilter field_22232_o = new MouseFilter();
        private MouseFilter field_22231_p = new MouseFilter();
        private MouseFilter field_22229_q = new MouseFilter();
        private float field_22228_r = 4.0F;
        private float field_22227_s = 4.0F;
        private float field_22226_t = 0.0F;
        private float field_22225_u = 0.0F;
        private float field_22224_v = 0.0F;
        private float field_22223_w = 0.0F;
        private float field_22222_x = 0.0F;
        private float field_22221_y = 0.0F;
        private float field_22220_z = 0.0F;
        private float field_22230_A = 0.0F;
        private boolean field_27288_B = false;
        private double cameraZoom = 1.0D;
        private double cameraYaw = 0.0D;
        private double cameraPitch = 0.0D;
        private long prevFrameTime = System.currentTimeMillis();
        private Random random = new Random();
        private int field_27287_H = 0;
        volatile int field_1394_b = 0;
        volatile int field_1393_c = 0;
        FloatBuffer field_1392_d = GLAllocation.createDirectFloatBuffer(16);
        float fogColorRed;
        float fogColorGreen;
        float fogColorBlue;
        private float field_1382_n;
        private float field_1381_o;
        private int nanTickCounter = 0;
        private float lightPulsePhase = 0.0F;
        private boolean redFogActive = false;
        private int redFogTimer = 0;
        private long vhsStartTime = System.currentTimeMillis();
        private float vhsIntensity = 1.0F;

        public EntityRenderer(Minecraft var1) {
                this.mc = var1;
                this.itemRenderer = new ItemRenderer(var1);
        }

        public void updateRenderer() {
                this.field_1382_n = this.field_1381_o;
                this.field_22227_s = this.field_22228_r;
                this.field_22225_u = this.field_22226_t;
                this.field_22223_w = this.field_22224_v;
                this.field_22221_y = this.field_22222_x;
                this.field_22230_A = this.field_22220_z;
                if(this.mc.renderViewEntity == null) {
                        this.mc.renderViewEntity = this.mc.thePlayer;
                }

                float var1 = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(this.mc.renderViewEntity.posX), MathHelper.floor_double(this.mc.renderViewEntity.posY), MathHelper.floor_double(this.mc.renderViewEntity.posZ));
                float var2 = (float)(3 - this.mc.gameSettings.renderDistance) / 3.0F;
                float var3 = var1 * (1.0F - var2) + var2;
                this.field_1381_o += (var3 - this.field_1381_o) * 0.1F;
                ++this.field_1386_j;
                ++this.nanTickCounter;
                this.lightPulsePhase += 0.15F;

                // Update red fog timer for mystery events
                if(this.redFogActive) {
                        ++this.redFogTimer;
                        if(this.redFogTimer >= 600) { // Red fog lasts ~30 seconds (600 ticks at 20tps)
                                this.redFogActive = false;
                                this.redFogTimer = 0;
                        }
                }

                // Update VHS intensity - increases every 90 seconds
                long elapsed = System.currentTimeMillis() - this.vhsStartTime;
                this.vhsIntensity = 1.0F + (float)(elapsed / 90000L);

                this.itemRenderer.updateEquippedItem();
                this.addRainParticles();
        }

        public void setRedFog(boolean active) {
                this.redFogActive = active;
                if(active) {
                        this.redFogTimer = 0;
                }
        }

        public void getMouseOver(float var1) {
                if(this.mc.renderViewEntity != null) {
                        if(this.mc.theWorld != null) {
                                double var2 = (double)this.mc.playerController.getBlockReachDistance();
                                this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(var2, var1);
                                double var4 = var2;
                                Vec3D var6 = this.mc.renderViewEntity.getPosition(var1);
                                if(this.mc.objectMouseOver != null) {
                                        var4 = this.mc.objectMouseOver.hitVec.distanceTo(var6);
                                }

                                if(this.mc.playerController instanceof PlayerControllerTest) {
                                        var2 = 32.0D;
                                } else {
                                        if(var4 > 3.0D) {
                                                var4 = 3.0D;
                                        }

                                        var2 = var4;
                                }

                                Vec3D var7 = this.mc.renderViewEntity.getLook(var1);
                                Vec3D var8 = var6.addVector(var7.xCoord * var2, var7.yCoord * var2, var7.zCoord * var2);
                                this.field_1385_k = null;
                                float var9 = 1.0F;
                                List var10 = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(var7.xCoord * var2, var7.yCoord * var2, var7.zCoord * var2).expand((double)var9, (double)var9, (double)var9));
                                double var11 = 0.0D;

                                for(int var13 = 0; var13 < var10.size(); ++var13) {
                                        Entity var14 = (Entity)var10.get(var13);
                                        if(var14.canBeCollidedWith()) {
                                                float var15 = var14.getCollisionBorderSize();
                                                AxisAlignedBB var16 = var14.boundingBox.expand((double)var15, (double)var15, (double)var15);
                                                MovingObjectPosition var17 = var16.func_1169_a(var6, var8);
                                                if(var16.isVecInside(var6)) {
                                                        if(0.0D < var11 || var11 == 0.0D) {
                                                                this.field_1385_k = var14;
                                                                var11 = 0.0D;
                                                        }
                                                } else if(var17 != null) {
                                                        double var18 = var6.distanceTo(var17.hitVec);
                                                        if(var18 < var11 || var11 == 0.0D) {
                                                                this.field_1385_k = var14;
                                                                var11 = var18;
                                                        }
                                                }
                                        }
                                }

                                if(this.field_1385_k != null && !(this.mc.playerController instanceof PlayerControllerTest)) {
                                        this.mc.objectMouseOver = new MovingObjectPosition(this.field_1385_k);
                                }

                        }
                }
        }

        private float getFOVModifier(float var1) {
                EntityLiving var2 = this.mc.renderViewEntity;
                float var3 = 70.0F;
                if(var2.isInsideOfMaterial(Material.water)) {
                        var3 = 60.0F;
                }

                if(var2.health <= 0) {
                        float var4 = (float)var2.deathTime + var1;
                        var3 /= (1.0F - 500.0F / (var4 + 500.0F)) * 2.0F + 1.0F;
                }

                return var3 + this.field_22221_y + (this.field_22222_x - this.field_22221_y) * var1;
        }

        private void hurtCameraEffect(float var1) {
                EntityLiving var2 = this.mc.renderViewEntity;
                float var3 = (float)var2.hurtTime - var1;
                float var4;
                if(var2.health <= 0) {
                        var4 = (float)var2.deathTime + var1;
                        GL11.glRotatef(40.0F - 8000.0F / (var4 + 200.0F), 0.0F, 0.0F, 1.0F);
                }

                if(var3 >= 0.0F) {
                        var3 /= (float)var2.maxHurtTime;
                        var3 = MathHelper.sin(var3 * var3 * var3 * var3 * (float)Math.PI);
                        var4 = var2.attackedAtYaw;
                        GL11.glRotatef(-var4, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(-var3 * 14.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glRotatef(var4, 0.0F, 1.0F, 0.0F);
                }
        }

        private void setupViewBobbing(float var1) {
                if(this.mc.renderViewEntity instanceof EntityPlayer) {
                        EntityPlayer var2 = (EntityPlayer)this.mc.renderViewEntity;
                        float var3 = var2.distanceWalkedModified - var2.prevDistanceWalkedModified;
                        float var4 = -(var2.distanceWalkedModified + var3 * var1);
                        float var5 = var2.field_775_e + (var2.field_774_f - var2.field_775_e) * var1;
                        float var6 = var2.cameraPitch + (var2.field_9328_R - var2.cameraPitch) * var1;
                        GL11.glTranslatef(MathHelper.sin(var4 * (float)Math.PI) * var5 * 0.5F, -Math.abs(MathHelper.cos(var4 * (float)Math.PI) * var5), 0.0F);
                        GL11.glRotatef(MathHelper.sin(var4 * (float)Math.PI) * var5 * 3.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glRotatef(Math.abs(MathHelper.cos(var4 * (float)Math.PI - 0.2F) * var5) * 5.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(var6, 1.0F, 0.0F, 0.0F);
                }
        }

        private void orientCamera(float var1) {
                EntityLiving var2 = this.mc.renderViewEntity;
                float var3 = var2.yOffset - 1.62F;
                double var4 = var2.prevPosX + (var2.posX - var2.prevPosX) * (double)var1;
                double var6 = var2.prevPosY + (var2.posY - var2.prevPosY) * (double)var1 - (double)var3;
                double var8 = var2.prevPosZ + (var2.posZ - var2.prevPosZ) * (double)var1;
                GL11.glRotatef(this.field_22230_A + (this.field_22220_z - this.field_22230_A) * var1, 0.0F, 0.0F, 1.0F);
                if(var2.isPlayerSleeping()) {
                        var3 = (float)((double)var3 + 1.0D);
                        GL11.glTranslatef(0.0F, 0.3F, 0.0F);
                        if(!this.mc.gameSettings.field_22273_E) {
                                int var10 = this.mc.theWorld.getBlockId(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                                if(var10 == Block.blockBed.blockID) {
                                        int var11 = this.mc.theWorld.getBlockMetadata(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                                        int var12 = var11 & 3;
                                        GL11.glRotatef((float)(var12 * 90), 0.0F, 1.0F, 0.0F);
                                }

                                GL11.glRotatef(var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1 + 180.0F, 0.0F, -1.0F, 0.0F);
                                GL11.glRotatef(var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1, -1.0F, 0.0F, 0.0F);
                        }
                } else if(this.mc.gameSettings.thirdPersonView) {
                        double var27 = (double)(this.field_22227_s + (this.field_22228_r - this.field_22227_s) * var1);
                        float var13;
                        float var28;
                        if(this.mc.gameSettings.field_22273_E) {
                                var28 = this.field_22225_u + (this.field_22226_t - this.field_22225_u) * var1;
                                var13 = this.field_22223_w + (this.field_22224_v - this.field_22223_w) * var1;
                                GL11.glTranslatef(0.0F, 0.0F, (float)(-var27));
                                GL11.glRotatef(var13, 1.0F, 0.0F, 0.0F);
                                GL11.glRotatef(var28, 0.0F, 1.0F, 0.0F);
                        } else {
                                var28 = var2.rotationYaw;
                                var13 = var2.rotationPitch;
                                double var14 = (double)(-MathHelper.sin(var28 / 180.0F * (float)Math.PI) * MathHelper.cos(var13 / 180.0F * (float)Math.PI)) * var27;
                                double var16 = (double)(MathHelper.cos(var28 / 180.0F * (float)Math.PI) * MathHelper.cos(var13 / 180.0F * (float)Math.PI)) * var27;
                                double var18 = (double)(-MathHelper.sin(var13 / 180.0F * (float)Math.PI)) * var27;

                                for(int var20 = 0; var20 < 8; ++var20) {
                                        float var21 = (float)((var20 & 1) * 2 - 1);
                                        float var22 = (float)((var20 >> 1 & 1) * 2 - 1);
                                        float var23 = (float)((var20 >> 2 & 1) * 2 - 1);
                                        var21 *= 0.1F;
                                        var22 *= 0.1F;
                                        var23 *= 0.1F;
                                        MovingObjectPosition var24 = this.mc.theWorld.rayTraceBlocks(Vec3D.createVector(var4 + (double)var21, var6 + (double)var22, var8 + (double)var23), Vec3D.createVector(var4 - var14 + (double)var21 + (double)var23, var6 - var18 + (double)var22, var8 - var16 + (double)var23));
                                        if(var24 != null) {
                                                double var25 = var24.hitVec.distanceTo(Vec3D.createVector(var4, var6, var8));
                                                if(var25 < var27) {
                                                        var27 = var25;
                                                }
                                        }
                                }

                                GL11.glRotatef(var2.rotationPitch - var13, 1.0F, 0.0F, 0.0F);
                                GL11.glRotatef(var2.rotationYaw - var28, 0.0F, 1.0F, 0.0F);
                                GL11.glTranslatef(0.0F, 0.0F, (float)(-var27));
                                GL11.glRotatef(var28 - var2.rotationYaw, 0.0F, 1.0F, 0.0F);
                                GL11.glRotatef(var13 - var2.rotationPitch, 1.0F, 0.0F, 0.0F);
                        }
                } else {
                        GL11.glTranslatef(0.0F, 0.0F, -0.1F);
                }

                if(!this.mc.gameSettings.field_22273_E) {
                        GL11.glRotatef(var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1 + 180.0F, 0.0F, 1.0F, 0.0F);
                }

                GL11.glTranslatef(0.0F, var3, 0.0F);
                var4 = var2.prevPosX + (var2.posX - var2.prevPosX) * (double)var1;
                var6 = var2.prevPosY + (var2.posY - var2.prevPosY) * (double)var1 - (double)var3;
                var8 = var2.prevPosZ + (var2.posZ - var2.prevPosZ) * (double)var1;
                this.field_27288_B = this.mc.renderGlobal.func_27307_a(var4, var6, var8, var1);
        }

        private void setupCameraTransform(float var1, int var2) {
                this.farPlaneDistance = (float)(256 >> this.mc.gameSettings.renderDistance);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                float var3 = 0.07F;
                if(this.mc.gameSettings.anaglyph) {
                        GL11.glTranslatef((float)(-(var2 * 2 - 1)) * var3, 0.0F, 0.0F);
                }

                if(this.cameraZoom != 1.0D) {
                        GL11.glTranslatef((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
                        GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
                        GLU.gluPerspective(this.getFOVModifier(var1), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance);
                } else {
                        GLU.gluPerspective(this.getFOVModifier(var1), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance);
                }

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                if(this.mc.gameSettings.anaglyph) {
                        GL11.glTranslatef((float)(var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
                }

                this.hurtCameraEffect(var1);
                if(this.mc.gameSettings.viewBobbing) {
                        this.setupViewBobbing(var1);
                }

                // Strong camera shake during final event
                if(this.mc.finalEventActive) {
                        long elapsedMs = System.currentTimeMillis() - this.mc.finalEventStartTime;
                        if(elapsedMs < 19000) { // Shake for first 19 seconds (before crash)
                                float shakeIntensity = 2.0F + (this.mc.random.nextFloat() * 3.0F);
                                float shakeX = (this.mc.random.nextFloat() - 0.5F) * shakeIntensity;
                                float shakeY = (this.mc.random.nextFloat() - 0.5F) * shakeIntensity;
                                float shakeZ = (this.mc.random.nextFloat() - 0.5F) * shakeIntensity;
                                GL11.glTranslatef(shakeX, shakeY, shakeZ);
                        }
                }

                float var4 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * var1;
                if(var4 > 0.0F) {
                        float var5 = 5.0F / (var4 * var4 + 5.0F) - var4 * 0.04F;
                        var5 *= var5;
                        GL11.glRotatef(var4 * var4 * 1500.0F, 0.0F, 1.0F, 1.0F);
                        GL11.glScalef(1.0F / var5, 1.0F, 1.0F);
                        GL11.glRotatef(-var4 * var4 * 1500.0F, 0.0F, 1.0F, 1.0F);
                }

                this.orientCamera(var1);
        }

        private void func_4135_b(float var1, int var2) {
                GL11.glLoadIdentity();
                if(this.mc.gameSettings.anaglyph) {
                        GL11.glTranslatef((float)(var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
                }

                GL11.glPushMatrix();
                this.hurtCameraEffect(var1);
                if(this.mc.gameSettings.viewBobbing) {
                        this.setupViewBobbing(var1);
                }

                if(!this.mc.gameSettings.thirdPersonView && !this.mc.renderViewEntity.isPlayerSleeping() && !this.mc.gameSettings.hideGUI) {
                        this.itemRenderer.renderItemInFirstPerson(var1);
                }

                GL11.glPopMatrix();
                if(!this.mc.gameSettings.thirdPersonView && !this.mc.renderViewEntity.isPlayerSleeping()) {
                        this.itemRenderer.renderOverlays(var1);
                        this.hurtCameraEffect(var1);
                }

                if(this.mc.gameSettings.viewBobbing) {
                        this.setupViewBobbing(var1);
                }

        }

        public void updateCameraAndRender(float var1) {
                if(!Display.isActive()) {
                        if(System.currentTimeMillis() - this.prevFrameTime > 500L) {
                                this.mc.displayInGameMenu();
                        }
                } else {
                        this.prevFrameTime = System.currentTimeMillis();
                }

                if(this.mc.inGameHasFocus) {
                        this.mc.mouseHelper.mouseXYChange();
                        float var2 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                        float var3 = var2 * var2 * var2 * 8.0F;
                        float var4 = (float)this.mc.mouseHelper.deltaX * var3;
                        float var5 = (float)this.mc.mouseHelper.deltaY * var3;
                        byte var6 = 1;
                        if(this.mc.gameSettings.invertMouse) {
                                var6 = -1;
                        }

                        if(this.mc.gameSettings.smoothCamera) {
                                var4 = this.mouseFilterXAxis.func_22386_a(var4, 0.05F * var3);
                                var5 = this.mouseFilterYAxis.func_22386_a(var5, 0.05F * var3);
                        }

                        this.mc.thePlayer.func_346_d(var4, var5 * (float)var6);
                }

                if(!this.mc.field_6307_v) {
                        ScaledResolution var7 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
                        int var8 = var7.getScaledWidth();
                        int var9 = var7.getScaledHeight();
                        int var10 = Mouse.getX() * var8 / this.mc.displayWidth;
                        int var11 = var9 - Mouse.getY() * var9 / this.mc.displayHeight - 1;
                        if(this.mc.theWorld != null) {
                                this.renderWorld(var1);
                                if(!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null) {
                                        this.mc.ingameGUI.renderGameOverlay(var1, this.mc.currentScreen != null, var10, var11);
                                }
                        } else {
                                GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                                GL11.glMatrixMode(GL11.GL_PROJECTION);
                                GL11.glLoadIdentity();
                                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                                GL11.glLoadIdentity();
                                this.func_905_b();
                        }

                        if(this.mc.currentScreen != null) {
                                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                                this.mc.currentScreen.drawScreen(var10, var11, var1);
                                if(this.mc.currentScreen != null && this.mc.currentScreen.field_25091_h != null) {
                                        this.mc.currentScreen.field_25091_h.func_25087_a(var1);
                                }
                        }

                        // VHS Effect
                        this.renderVHSEffect();
                        this.renderScreenDistortion();
                        this.renderFogEdgePixels();

                }
        }

        public void renderWorld(float var1) {
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                if(this.mc.renderViewEntity == null) {
                        this.mc.renderViewEntity = this.mc.thePlayer;
                }

                this.getMouseOver(var1);
                EntityLiving var2 = this.mc.renderViewEntity;
                RenderGlobal var3 = this.mc.renderGlobal;
                EffectRenderer var4 = this.mc.effectRenderer;
                double var5 = var2.lastTickPosX + (var2.posX - var2.lastTickPosX) * (double)var1;
                double var7 = var2.lastTickPosY + (var2.posY - var2.lastTickPosY) * (double)var1;
                double var9 = var2.lastTickPosZ + (var2.posZ - var2.lastTickPosZ) * (double)var1;
                IChunkProvider var11 = this.mc.theWorld.getIChunkProvider();
                int var14;
                if(var11 instanceof ChunkProviderLoadOrGenerate) {
                        ChunkProviderLoadOrGenerate var12 = (ChunkProviderLoadOrGenerate)var11;
                        int var13 = MathHelper.floor_float((float)((int)var5)) >> 4;
                        var14 = MathHelper.floor_float((float)((int)var9)) >> 4;
                        var12.setCurrentChunkOver(var13, var14);
                }

                for(int var15 = 0; var15 < 2; ++var15) {
                        if(this.mc.gameSettings.anaglyph) {
                                if(var15 == 0) {
                                        GL11.glColorMask(false, true, true, false);
                                } else {
                                        GL11.glColorMask(true, false, false, false);
                                }
                        }

                        GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                        this.updateFogColor(var1);
                        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
                        GL11.glEnable(GL11.GL_CULL_FACE);
                        this.setupCameraTransform(var1, var15);
                        ClippingHelperImpl.getInstance();
                        if(this.mc.gameSettings.renderDistance < 2) {
                                this.setupFog(-1, var1);
                                var3.renderSky(var1);
                        }

                        GL11.glEnable(GL11.GL_FOG);
                        this.setupFog(1, var1);
                        if(this.mc.gameSettings.ambientOcclusion) {
                                GL11.glShadeModel(GL11.GL_SMOOTH);
                        }

                        Frustrum var16 = new Frustrum();
                        var16.setPosition(var5, var7, var9);
                        this.mc.renderGlobal.clipRenderersByFrustrum(var16, var1);
                        this.mc.renderGlobal.updateRenderers(var2, false);
                        this.setupFog(0, var1);
                        GL11.glEnable(GL11.GL_FOG);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));
                        RenderHelper.disableStandardItemLighting();
                        var3.sortAndRender(var2, 0, (double)var1);
                        GL11.glShadeModel(GL11.GL_FLAT);
                        RenderHelper.enableStandardItemLighting();
                        var3.renderEntities(var2.getPosition(var1), var16, var1);
                        var4.func_1187_b(var2, var1);
                        RenderHelper.disableStandardItemLighting();
                        this.setupFog(0, var1);
                        var4.renderParticles(var2, var1);
                        EntityPlayer var17;
                        if(this.mc.objectMouseOver != null && var2.isInsideOfMaterial(Material.water) && var2 instanceof EntityPlayer) {
                                var17 = (EntityPlayer)var2;
                                GL11.glDisable(GL11.GL_ALPHA_TEST);
                                var3.func_959_a(var17, this.mc.objectMouseOver, 0, var17.inventory.getCurrentItem(), var1);
                                var3.drawSelectionBox(var17, this.mc.objectMouseOver, 0, var17.inventory.getCurrentItem(), var1);
                                GL11.glEnable(GL11.GL_ALPHA_TEST);
                        }

                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        this.setupFog(0, var1);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glDisable(GL11.GL_CULL_FACE);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));
                        if(this.mc.gameSettings.fancyGraphics) {
                                if(this.mc.gameSettings.ambientOcclusion) {
                                        GL11.glShadeModel(GL11.GL_SMOOTH);
                                }

                                GL11.glColorMask(false, false, false, false);
                                var14 = var3.sortAndRender(var2, 1, (double)var1);
                                GL11.glColorMask(true, true, true, true);
                                if(this.mc.gameSettings.anaglyph) {
                                        if(var15 == 0) {
                                                GL11.glColorMask(false, true, true, false);
                                        } else {
                                                GL11.glColorMask(true, false, false, false);
                                        }
                                }

                                if(var14 > 0) {
                                        var3.func_944_a(1, (double)var1);
                                }

                                GL11.glShadeModel(GL11.GL_FLAT);
                        } else {
                                var3.sortAndRender(var2, 1, (double)var1);
                        }

                        GL11.glDepthMask(true);
                        GL11.glEnable(GL11.GL_CULL_FACE);
                        GL11.glDisable(GL11.GL_BLEND);
                        if(this.cameraZoom == 1.0D && var2 instanceof EntityPlayer && this.mc.objectMouseOver != null && !var2.isInsideOfMaterial(Material.water)) {
                                var17 = (EntityPlayer)var2;
                                GL11.glDisable(GL11.GL_ALPHA_TEST);
                                var3.func_959_a(var17, this.mc.objectMouseOver, 0, var17.inventory.getCurrentItem(), var1);
                                var3.drawSelectionBox(var17, this.mc.objectMouseOver, 0, var17.inventory.getCurrentItem(), var1);
                                GL11.glEnable(GL11.GL_ALPHA_TEST);
                        }

                        this.func_27286_d(var1);
                        GL11.glDisable(GL11.GL_FOG);
                        if(this.field_1385_k != null) {
                        }

                        this.setupFog(0, var1);
                        GL11.glEnable(GL11.GL_FOG);
                        var3.renderClouds(var1);
                        GL11.glDisable(GL11.GL_FOG);
                        this.setupFog(1, var1);
                        if(this.cameraZoom == 1.0D) {
                                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                                this.func_4135_b(var1, var15);
                        }

                        if(!this.mc.gameSettings.anaglyph) {
                                return;
                        }
                }

                GL11.glColorMask(true, true, true, false);
        }

        private void addRainParticles() {
                if(this.mc.gameSettings.fancyGraphics) {
                        float var1 = this.mc.theWorld.func_27162_g(1.0F);
                        if(var1 != 0.0F) {
                                this.random.setSeed((long)this.field_1386_j * 312987231L);
                                EntityLiving var2 = this.mc.renderViewEntity;
                                World var3 = this.mc.theWorld;
                                int var4 = MathHelper.floor_double(var2.posX);
                                int var5 = MathHelper.floor_double(var2.posY);
                                int var6 = MathHelper.floor_double(var2.posZ);
                                byte var7 = 10;
                                double var8 = 0.0D;
                                double var10 = 0.0D;
                                double var12 = 0.0D;
                                int var14 = 0;

                                for(int var15 = 0; var15 < (int)(100.0F * var1 * var1); ++var15) {
                                        int var16 = var4 + this.random.nextInt(var7) - this.random.nextInt(var7);
                                        int var17 = var6 + this.random.nextInt(var7) - this.random.nextInt(var7);
                                        int var18 = var3.findTopSolidBlock(var16, var17);
                                        int var19 = var3.getBlockId(var16, var18 - 1, var17);
                                        if(var18 <= var5 + var7 && var18 >= var5 - var7 && var3.getWorldChunkManager().func_4073_a(var16, var17).func_27077_d()) {
                                                float var20 = this.random.nextFloat();
                                                float var21 = this.random.nextFloat();
                                                if(var19 > 0) {
                                                        if(Block.blocksList[var19].blockMaterial == Material.lava) {
                                                                this.mc.effectRenderer.addEffect(new EntitySmokeFX(var3, (double)((float)var16 + var20), (double)((float)var18 + 0.1F) - Block.blocksList[var19].minY, (double)((float)var17 + var21), 0.0D, 0.0D, 0.0D));
                                                        } else {
                                                                ++var14;
                                                                if(this.random.nextInt(var14) == 0) {
                                                                        var8 = (double)((float)var16 + var20);
                                                                        var10 = (double)((float)var18 + 0.1F) - Block.blocksList[var19].minY;
                                                                        var12 = (double)((float)var17 + var21);
                                                                }

                                                                this.mc.effectRenderer.addEffect(new EntityRainFX(var3, (double)((float)var16 + var20), (double)((float)var18 + 0.1F) - Block.blocksList[var19].minY, (double)((float)var17 + var21)));
                                                        }
                                                }
                                        }
                                }

                                if(var14 > 0 && this.random.nextInt(3) < this.field_27287_H++) {
                                        this.field_27287_H = 0;
                                        if(var10 > var2.posY + 2.0D && !var3.canBlockSeeTheSky(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ))) {
                                                this.mc.theWorld.playSoundEffect(var8, var10, var12, "ambient.weather.rain", 0.1F, 0.5F);
                                        } else {
                                                this.mc.theWorld.playSoundEffect(var8, var10, var12, "ambient.weather.rain", 0.2F, 1.0F);
                                        }
                                }

                        }
                }
        }

        protected void func_27286_d(float var1) {
                float var2 = this.mc.theWorld.func_27162_g(var1);
                if(var2 > 0.0F) {
                        EntityLiving var3 = this.mc.renderViewEntity;
                        World var4 = this.mc.theWorld;
                        int var5 = MathHelper.floor_double(var3.posX);
                        int var6 = MathHelper.floor_double(var3.posY);
                        int var7 = MathHelper.floor_double(var3.posZ);
                        Tessellator var8 = Tessellator.instance;
                        GL11.glDisable(GL11.GL_CULL_FACE);
                        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/environment/snow.png"));
                        double var9 = var3.lastTickPosX + (var3.posX - var3.lastTickPosX) * (double)var1;
                        double var11 = var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)var1;
                        double var13 = var3.lastTickPosZ + (var3.posZ - var3.lastTickPosZ) * (double)var1;
                        byte var15 = 5;
                        if(this.mc.gameSettings.fancyGraphics) {
                                var15 = 10;
                        }

                        BiomeGenBase[] var16 = var4.getWorldChunkManager().func_4069_a(var5 - var15, var7 - var15, var15 * 2 + 1, var15 * 2 + 1);
                        int var17 = 0;

                        int var18;
                        int var19;
                        BiomeGenBase var20;
                        int var21;
                        int var22;
                        int var23;
                        float var24;
                        float var25;
                        for(var18 = var5 - var15; var18 <= var5 + var15; ++var18) {
                                for(var19 = var7 - var15; var19 <= var7 + var15; ++var19) {
                                        var20 = var16[var17++];
                                        if(var20.func_27078_c()) {
                                                var21 = var4.findTopSolidBlock(var18, var19);
                                                if(var21 < 0) {
                                                        var21 = 0;
                                                }

                                                var22 = var6 - var15;
                                                var23 = var6 + var15;
                                                if(var22 < var21) {
                                                        var22 = var21;
                                                }

                                                if(var23 < var21) {
                                                        var23 = var21;
                                                }

                                                var24 = 1.0F;
                                                if(var22 != var23) {
                                                        this.random.setSeed((long)(var18 * var18 * 3121 + var18 * 45238971 + var19 * var19 * 418711 + var19 * 13761));
                                                        var25 = (float)this.field_1386_j + var1;
                                                        float var26 = ((float)(this.field_1386_j & 511) + var1) / 512.0F;
                                                        float var27 = this.random.nextFloat() + var25 * 0.01F * (float)this.random.nextGaussian();
                                                        float var28 = this.random.nextFloat() + var25 * (float)this.random.nextGaussian() * 0.001F;
                                                        double var29 = (double)((float)var18 + 0.5F) - var3.posX;
                                                        double var31 = (double)((float)var19 + 0.5F) - var3.posZ;
                                                        float var33 = MathHelper.sqrt_double(var29 * var29 + var31 * var31) / (float)var15;
                                                        var8.startDrawingQuads();
                                                        float var34 = var4.getLightBrightness(var18, 128, var19);
                                                        GL11.glColor4f(var34, var34, var34, ((1.0F - var33 * var33) * 0.3F + 0.5F) * var2);
                                                        var8.setTranslationD(-var9 * 1.0D, -var11 * 1.0D, -var13 * 1.0D);
                                                        var8.addVertexWithUV((double)(var18 + 0), (double)var22, (double)var19 + 0.5D, (double)(0.0F * var24 + var27), (double)((float)var22 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)(var18 + 1), (double)var22, (double)var19 + 0.5D, (double)(1.0F * var24 + var27), (double)((float)var22 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)(var18 + 1), (double)var23, (double)var19 + 0.5D, (double)(1.0F * var24 + var27), (double)((float)var23 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)(var18 + 0), (double)var23, (double)var19 + 0.5D, (double)(0.0F * var24 + var27), (double)((float)var23 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var22, (double)(var19 + 0), (double)(0.0F * var24 + var27), (double)((float)var22 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var22, (double)(var19 + 1), (double)(1.0F * var24 + var27), (double)((float)var22 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var23, (double)(var19 + 1), (double)(1.0F * var24 + var27), (double)((float)var23 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var23, (double)(var19 + 0), (double)(0.0F * var24 + var27), (double)((float)var23 * var24 / 4.0F + var26 * var24 + var28));
                                                        var8.setTranslationD(0.0D, 0.0D, 0.0D);
                                                        var8.draw();
                                                }
                                        }
                                }
                        }

                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/environment/rain.png"));
                        if(this.mc.gameSettings.fancyGraphics) {
                                var15 = 10;
                        }

                        var17 = 0;

                        for(var18 = var5 - var15; var18 <= var5 + var15; ++var18) {
                                for(var19 = var7 - var15; var19 <= var7 + var15; ++var19) {
                                        var20 = var16[var17++];
                                        if(var20.func_27077_d()) {
                                                var21 = var4.findTopSolidBlock(var18, var19);
                                                var22 = var6 - var15;
                                                var23 = var6 + var15;
                                                if(var22 < var21) {
                                                        var22 = var21;
                                                }

                                                if(var23 < var21) {
                                                        var23 = var21;
                                                }

                                                var24 = 2.0F;
                                                if(var22 != var23) {
                                                        var25 = ((float)(this.field_1386_j + var18 * var18 * 3121 + var18 * 45238971 + var19 * var19 * 418711 + var19 * 13761 & 31) + var1) / 32.0F;
                                                        double var35 = (double)((float)var18 + 0.5F) - var3.posX;
                                                        double var36 = (double)((float)var19 + 0.5F) - var3.posZ;
                                                        float var30 = MathHelper.sqrt_double(var35 * var35 + var36 * var36) / (float)var15;
                                                        var8.startDrawingQuads();
                                                        float var37 = var4.getLightBrightness(var18, 128, var19) * 0.85F + 0.15F;
                                                        GL11.glColor4f(var37, var37, var37, ((1.0F - var30 * var30) * 0.5F + 0.5F) * var2);
                                                        var8.setTranslationD(-var9 * 1.0D, -var11 * 1.0D, -var13 * 1.0D);
                                                        var8.addVertexWithUV((double)(var18 + 0), (double)var22, (double)var19 + 0.5D, (double)(0.0F * var24), (double)((float)var22 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)(var18 + 1), (double)var22, (double)var19 + 0.5D, (double)(1.0F * var24), (double)((float)var22 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)(var18 + 1), (double)var23, (double)var19 + 0.5D, (double)(1.0F * var24), (double)((float)var23 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)(var18 + 0), (double)var23, (double)var19 + 0.5D, (double)(0.0F * var24), (double)((float)var23 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var22, (double)(var19 + 0), (double)(0.0F * var24), (double)((float)var22 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var22, (double)(var19 + 1), (double)(1.0F * var24), (double)((float)var22 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var23, (double)(var19 + 1), (double)(1.0F * var24), (double)((float)var23 * var24 / 4.0F + var25 * var24));
                                                        var8.addVertexWithUV((double)var18 + 0.5D, (double)var23, (double)(var19 + 0), (double)(0.0F * var24), (double)((float)var23 * var24 / 4.0F + var25 * var24));
                                                        var8.setTranslationD(0.0D, 0.0D, 0.0D);
                                                        var8.draw();
                                                }
                                        }
                                }
                        }

                        GL11.glEnable(GL11.GL_CULL_FACE);
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
                }
        }

        public void func_905_b() {
                ScaledResolution var1 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(0.0D, var1.field_25121_a, var1.field_25120_b, 0.0D, 1000.0D, 3000.0D);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        }

        private void updateFogColor(float var1) {
                World var2 = this.mc.theWorld;
                EntityLiving var3 = this.mc.renderViewEntity;
                float var4 = 1.0F / (float)(4 - this.mc.gameSettings.renderDistance);
                var4 = 1.0F - (float)Math.pow((double)var4, 0.25D);
                Vec3D var5 = var2.func_4079_a(this.mc.renderViewEntity, var1);
                float var6 = (float)var5.xCoord;
                float var7 = (float)var5.yCoord;
                float var8 = (float)var5.zCoord;
                Vec3D var9 = var2.getFogColor(var1);
                this.fogColorRed = (float)var9.xCoord;
                this.fogColorGreen = (float)var9.yCoord;
                this.fogColorBlue = (float)var9.zCoord;
                this.fogColorRed += (var6 - this.fogColorRed) * var4;
                this.fogColorGreen += (var7 - this.fogColorGreen) * var4;
                this.fogColorBlue += (var8 - this.fogColorBlue) * var4;
                float var10 = var2.func_27162_g(var1);
                float var11;
                float var12;
                if(var10 > 0.0F) {
                        var11 = 1.0F - var10 * 0.5F;
                        var12 = 1.0F - var10 * 0.4F;
                        this.fogColorRed *= var11;
                        this.fogColorGreen *= var11;
                        this.fogColorBlue *= var12;
                }

                var11 = var2.func_27166_f(var1);
                if(var11 > 0.0F) {
                        var12 = 1.0F - var11 * 0.5F;
                        this.fogColorRed *= var12;
                        this.fogColorGreen *= var12;
                        this.fogColorBlue *= var12;
                }

                if(this.field_27288_B) {
                        Vec3D var16 = var2.func_628_d(var1);
                        this.fogColorRed = (float)var16.xCoord;
                        this.fogColorGreen = (float)var16.yCoord;
                        this.fogColorBlue = (float)var16.zCoord;
                } else if(var3.isInsideOfMaterial(Material.water)) {
                        this.fogColorRed = 0.02F;
                        this.fogColorGreen = 0.02F;
                        this.fogColorBlue = 0.2F;
                } else if(var3.isInsideOfMaterial(Material.lava)) {
                        this.fogColorRed = 0.6F;
                        this.fogColorGreen = 0.1F;
                        this.fogColorBlue = 0.0F;
                }

                var12 = this.field_1382_n + (this.field_1381_o - this.field_1382_n) * var1;
                this.fogColorRed *= var12;
                this.fogColorGreen *= var12;
                this.fogColorBlue *= var12;
                if(this.mc.gameSettings.anaglyph) {
                        float var13 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
                        float var14 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
                        float var15 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
                        this.fogColorRed = var13;
                        this.fogColorGreen = var14;
                        this.fogColorBlue = var15;
                }

                // NaN: Dark purple fog tint
                float purpleMix = 0.35F;
                this.fogColorRed = this.fogColorRed * (1.0F - purpleMix) + 0.08F * purpleMix;
                this.fogColorGreen = this.fogColorGreen * (1.0F - purpleMix) + 0.0F * purpleMix;
                this.fogColorBlue = this.fogColorBlue * (1.0F - purpleMix) + 0.18F * purpleMix;

                // Red fog override for mystery events
                if(this.redFogActive) {
                        float redIntensity = 0.7F + (float)Math.sin(this.lightPulsePhase) * 0.2F;
                        this.fogColorRed = redIntensity;
                        this.fogColorGreen = 0.1F;
                        this.fogColorBlue = 0.1F;
                }

                GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
        }

        private void setupFog(int var1, float var2) {
                EntityLiving var3 = this.mc.renderViewEntity;
                GL11.glFog(GL11.GL_FOG_COLOR, this.func_908_a(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
                GL11.glNormal3f(0.0F, -1.0F, 0.0F);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                float var4;
                float var5;
                float var6;
                float var7;
                float var8;
                float var9;
                if(this.field_27288_B) {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
                        var4 = 1.0F;
                        var5 = 1.0F;
                        var6 = 1.0F;
                        if(this.mc.gameSettings.anaglyph) {
                                var7 = (var4 * 30.0F + var5 * 59.0F + var6 * 11.0F) / 100.0F;
                                var8 = (var4 * 30.0F + var5 * 70.0F) / 100.0F;
                                var9 = (var4 * 30.0F + var6 * 70.0F) / 100.0F;
                        }
                } else if(var3.isInsideOfMaterial(Material.water)) {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
                        var4 = 0.4F;
                        var5 = 0.4F;
                        var6 = 0.9F;
                        if(this.mc.gameSettings.anaglyph) {
                                var7 = (var4 * 30.0F + var5 * 59.0F + var6 * 11.0F) / 100.0F;
                                var8 = (var4 * 30.0F + var5 * 70.0F) / 100.0F;
                                var9 = (var4 * 30.0F + var6 * 70.0F) / 100.0F;
                        }
                } else if(var3.isInsideOfMaterial(Material.lava)) {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                        GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
                        var4 = 0.4F;
                        var5 = 0.3F;
                        var6 = 0.3F;
                        if(this.mc.gameSettings.anaglyph) {
                                var7 = (var4 * 30.0F + var5 * 59.0F + var6 * 11.0F) / 100.0F;
                                var8 = (var4 * 30.0F + var5 * 70.0F) / 100.0F;
                                var9 = (var4 * 30.0F + var6 * 70.0F) / 100.0F;
                        }
                } else {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
                        GL11.glFogf(GL11.GL_FOG_START, this.farPlaneDistance * 0.25F);
                        GL11.glFogf(GL11.GL_FOG_END, this.farPlaneDistance);
                        if(var1 < 0) {
                                GL11.glFogf(GL11.GL_FOG_START, 0.0F);
                                GL11.glFogf(GL11.GL_FOG_END, this.farPlaneDistance * 0.8F);
                        }

                        if(GLContext.getCapabilities().GL_NV_fog_distance) {
                                GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV);
                        }

                        if(this.mc.theWorld.worldProvider.isNether) {
                                GL11.glFogf(GL11.GL_FOG_START, 0.0F);
                        }
                }

                GL11.glEnable(GL11.GL_COLOR_MATERIAL);
                GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
        }

        private FloatBuffer func_908_a(float var1, float var2, float var3, float var4) {
                this.field_1392_d.clear();
                this.field_1392_d.put(var1).put(var2).put(var3).put(var4);
                this.field_1392_d.flip();
                return this.field_1392_d;
        }

        private void renderScreenDistortion() {
                if(this.nanTickCounter % 400 < 20) {
                        ScaledResolution var1 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glDepthMask(false);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
                        GL11.glDisable(GL11.GL_ALPHA_TEST);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);

                        Tessellator var2 = Tessellator.instance;
                        float intensity = (float)(this.nanTickCounter % 400 - 0) / 20.0F;
                        if(intensity > 0.5F) {
                                intensity = 1.0F - intensity;
                        }
                        intensity *= 0.6F;

                        var2.startDrawingQuads();
                        var2.setColorRGBA_F(intensity, intensity, intensity, 1.0F);
                        var2.addVertex(0.0D, 0.0D, -90.0D);
                        var2.addVertex((double)this.mc.displayWidth, 0.0D, -90.0D);
                        var2.addVertex((double)this.mc.displayWidth, (double)this.mc.displayHeight, -90.0D);
                        var2.addVertex(0.0D, (double)this.mc.displayHeight, -90.0D);
                        var2.draw();

                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glEnable(GL11.GL_ALPHA_TEST);
                        GL11.glDepthMask(true);
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glDisable(GL11.GL_BLEND);
                }
        }

        private void renderFogEdgePixels() {
                if(this.random.nextInt(3) == 0) {
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glDepthMask(false);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);

                        Tessellator var1 = Tessellator.instance;
                        int edgeY = this.mc.displayHeight / 2;
                        for(int i = 0; i < 30; ++i) {
                                int px = this.random.nextInt(this.mc.displayWidth);
                                int py = this.random.nextInt(this.mc.displayHeight / 3) + this.mc.displayHeight / 3;
                                int size = this.random.nextInt(3) + 1;
                                int alpha = this.random.nextInt(120) + 30;

                                var1.startDrawingQuads();
                                var1.setColorRGBA_I(16777215, alpha);
                                var1.addVertex((double)px, (double)py, -90.0D);
                                var1.addVertex((double)(px + size), (double)py, -90.0D);
                                var1.addVertex((double)(px + size), (double)(py + size), -90.0D);
                                var1.addVertex((double)px, (double)(py + size), -90.0D);
                                var1.draw();
                        }

                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glDepthMask(true);
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glDisable(GL11.GL_BLEND);
                }
        }

        public float getLightPulse() {
                float pulse = (float)Math.sin((double)this.lightPulsePhase);
                float flicker = this.random.nextFloat() * 0.15F;
                return 0.7F + pulse * 0.2F + flicker;
        }

        public void renderVHSEffect() {
                ScaledResolution var1 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
                int var2 = var1.getScaledWidth();
                int var3 = var1.getScaledHeight();

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);

                Tessellator var4 = Tessellator.instance;
                long var5 = System.currentTimeMillis();

                // Scanlines - intensity increases over time
                int scanlineAlpha = (int)(30.0F * this.vhsIntensity);
                if(scanlineAlpha > 255) scanlineAlpha = 255;
                for(int var7 = 0; var7 < this.mc.displayHeight; var7 += 2) {
                        var4.startDrawingQuads();
                        var4.setColorRGBA_I(0, scanlineAlpha);
                        var4.addVertex(0.0D, (double)var7, -90.0D);
                        var4.addVertex((double)this.mc.displayWidth, (double)var7, -90.0D);
                        var4.addVertex((double)this.mc.displayWidth, (double)(var7 + 1), -90.0D);
                        var4.addVertex(0.0D, (double)(var7 + 1), -90.0D);
                        var4.draw();
                }

                // Random noise - more noise as intensity increases
                int noiseCount = (int)(50.0F * this.vhsIntensity);
                for(int var8 = 0; var8 < noiseCount; ++var8) {
                        int var9 = this.random.nextInt(this.mc.displayWidth);
                        int var10 = this.random.nextInt(this.mc.displayHeight);
                        int var11 = this.random.nextInt(20) + 5;
                        int var12 = this.random.nextInt(3) + 1;
                        int var13 = this.random.nextInt(100) + 50;

                        var4.startDrawingQuads();
                        var4.setColorRGBA_I(16777215, var13);
                        var4.addVertex((double)var9, (double)var10, -90.0D);
                        var4.addVertex((double)(var9 + var11), (double)var10, -90.0D);
                        var4.addVertex((double)(var9 + var11), (double)(var10 + var12), -90.0D);
                        var4.addVertex((double)var9, (double)(var10 + var12), -90.0D);
                        var4.draw();
                }

                // Horizontal glitch lines - more frequent as intensity increases
                int glitchChance = (int)(3.0F * this.vhsIntensity);
                if(this.random.nextInt(10) < glitchChance) {
                        int var14 = this.random.nextInt(this.mc.displayHeight);
                        int var15 = this.random.nextInt(5) + 2;
                        int var16 = this.random.nextInt(150) + 50;

                        var4.startDrawingQuads();
                        var4.setColorRGBA_I(16777215, var16);
                        var4.addVertex(0.0D, (double)var14, -90.0D);
                        var4.addVertex((double)this.mc.displayWidth, (double)var14, -90.0D);
                        var4.addVertex((double)this.mc.displayWidth, (double)(var14 + var15), -90.0D);
                        var4.addVertex(0.0D, (double)(var14 + var15), -90.0D);
                        var4.draw();
                }

                // Vignette effect - stronger as intensity increases
                int vignetteAlpha = (int)(100.0F * this.vhsIntensity);
                if(vignetteAlpha > 255) vignetteAlpha = 255;
                var4.startDrawingQuads();
                var4.setColorRGBA_I(0, vignetteAlpha);
                var4.addVertex(0.0D, 0.0D, -90.0D);
                var4.addVertex((double)this.mc.displayWidth, 0.0D, -90.0D);
                var4.setColorRGBA_I(0, 0);
                var4.addVertex((double)this.mc.displayWidth, (double)(this.mc.displayHeight / 4), -90.0D);
                var4.addVertex(0.0D, (double)(this.mc.displayHeight / 4), -90.0D);
                var4.draw();

                var4.startDrawingQuads();
                var4.setColorRGBA_I(0, 0);
                var4.addVertex(0.0D, (double)(this.mc.displayHeight * 3 / 4), -90.0D);
                var4.addVertex((double)this.mc.displayWidth, (double)(this.mc.displayHeight * 3 / 4), -90.0D);
                var4.setColorRGBA_I(0, vignetteAlpha);
                var4.addVertex((double)this.mc.displayWidth, (double)this.mc.displayHeight, -90.0D);
                var4.addVertex(0.0D, (double)this.mc.displayHeight, -90.0D);
                var4.draw();

                // Color aberration (chromatic aberration simulation) - stronger as intensity increases
                float var17 = (float)(Math.sin((double)var5 / 1000.0D) * 0.5D + 0.5D) * 0.1F * this.vhsIntensity;
                if(var17 > 1.0F) var17 = 1.0F;
                var4.startDrawingQuads();
                var4.setColorRGBA_F(1.0F, 0.0F, 0.0F, var17);
                var4.addVertex(0.0D, 0.0D, -90.0D);
                var4.addVertex((double)this.mc.displayWidth, 0.0D, -90.0D);
                var4.addVertex((double)this.mc.displayWidth, (double)this.mc.displayHeight, -90.0D);
                var4.addVertex(0.0D, (double)this.mc.displayHeight, -90.0D);
                var4.draw();

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_BLEND);
        }
}