package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatList {
	protected static Map field_25169_C = new HashMap();
	public static List field_25188_a = new ArrayList();
	public static List field_25187_b = new ArrayList();
	public static List field_25186_c = new ArrayList();
	public static List field_25185_d = new ArrayList();
	public static StatBase field_25184_e = (new StatBasic(1000, StatCollector.translateToLocal("stat.startGame"))).func_27082_h().func_25068_c();
	public static StatBase field_25183_f = (new StatBasic(1001, StatCollector.translateToLocal("stat.createWorld"))).func_27082_h().func_25068_c();
	public static StatBase field_25182_g = (new StatBasic(1002, StatCollector.translateToLocal("stat.loadWorld"))).func_27082_h().func_25068_c();
	public static StatBase field_25181_h = (new StatBasic(1003, StatCollector.translateToLocal("stat.joinMultiplayer"))).func_27082_h().func_25068_c();
	public static StatBase field_25180_i = (new StatBasic(1004, StatCollector.translateToLocal("stat.leaveGame"))).func_27082_h().func_25068_c();
	public static StatBase field_25179_j = (new StatBasic(1100, StatCollector.translateToLocal("stat.playOneMinute"), StatBase.field_27086_j)).func_27082_h().func_25068_c();
	public static StatBase field_25178_k = (new StatBasic(2000, StatCollector.translateToLocal("stat.walkOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_25177_l = (new StatBasic(2001, StatCollector.translateToLocal("stat.swimOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_25176_m = (new StatBasic(2002, StatCollector.translateToLocal("stat.fallOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_25175_n = (new StatBasic(2003, StatCollector.translateToLocal("stat.climbOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_25174_o = (new StatBasic(2004, StatCollector.translateToLocal("stat.flyOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_25173_p = (new StatBasic(2005, StatCollector.translateToLocal("stat.diveOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_27364_r = (new StatBasic(2006, StatCollector.translateToLocal("stat.minecartOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_27363_s = (new StatBasic(2007, StatCollector.translateToLocal("stat.boatOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_27362_t = (new StatBasic(2008, StatCollector.translateToLocal("stat.pigOneCm"), StatBase.field_27085_k)).func_27082_h().func_25068_c();
	public static StatBase field_25171_q = (new StatBasic(2010, StatCollector.translateToLocal("stat.jump"))).func_27082_h().func_25068_c();
	public static StatBase field_25168_r = (new StatBasic(2011, StatCollector.translateToLocal("stat.drop"))).func_27082_h().func_25068_c();
	public static StatBase field_25167_s = (new StatBasic(2020, StatCollector.translateToLocal("stat.damageDealt"))).func_25068_c();
	public static StatBase field_25165_t = (new StatBasic(2021, StatCollector.translateToLocal("stat.damageTaken"))).func_25068_c();
	public static StatBase field_25163_u = (new StatBasic(2022, StatCollector.translateToLocal("stat.deaths"))).func_25068_c();
	public static StatBase field_25162_v = (new StatBasic(2023, StatCollector.translateToLocal("stat.mobKills"))).func_25068_c();
	public static StatBase field_25161_w = (new StatBasic(2024, StatCollector.translateToLocal("stat.playerKills"))).func_25068_c();
	public static StatBase field_25160_x = (new StatBasic(2025, StatCollector.translateToLocal("stat.fishCaught"))).func_25068_c();
	public static StatBase[] field_25159_y = func_25153_a("stat.mineBlock", 16777216);
	public static StatBase[] field_25158_z = null;
	public static StatBase[] field_25172_A = null;
	public static StatBase[] field_25170_B = null;
	private static boolean field_25166_D;
	private static boolean field_25164_E;

	public static void func_27360_a() {
	}

	public static void func_25154_a() {
		field_25172_A = func_25155_a(field_25172_A, "stat.useItem", 16908288, 0, Block.blocksList.length);
		field_25170_B = func_25149_b(field_25170_B, "stat.breakItem", 16973824, 0, Block.blocksList.length);
		field_25166_D = true;
		func_25157_c();
	}

	public static void func_25151_b() {
		field_25172_A = func_25155_a(field_25172_A, "stat.useItem", 16908288, Block.blocksList.length, 32000);
		field_25170_B = func_25149_b(field_25170_B, "stat.breakItem", 16973824, Block.blocksList.length, 32000);
		field_25164_E = true;
		func_25157_c();
	}

	public static void func_25157_c() {
		if(field_25166_D && field_25164_E) {
			HashSet var0 = new HashSet();
			Iterator var1 = CraftingManager.getInstance().func_25193_b().iterator();

			while(var1.hasNext()) {
				IRecipe var2 = (IRecipe)var1.next();
				var0.add(Integer.valueOf(var2.func_25117_b().itemID));
			}

			var1 = FurnaceRecipes.smelting().func_25194_b().values().iterator();

			while(var1.hasNext()) {
				ItemStack var4 = (ItemStack)var1.next();
				var0.add(Integer.valueOf(var4.itemID));
			}

			field_25158_z = new StatBase[32000];
			var1 = var0.iterator();

			while(var1.hasNext()) {
				Integer var5 = (Integer)var1.next();
				if(Item.itemsList[var5.intValue()] != null) {
					String var3 = StatCollector.translateToLocalFormatted("stat.craftItem", new Object[]{Item.itemsList[var5.intValue()].func_25009_k()});
					field_25158_z[var5.intValue()] = (new StatCrafting(16842752 + var5.intValue(), var3, var5.intValue())).func_25068_c();
				}
			}

			replaceAllSimilarBlocks(field_25158_z);
		}
	}

	private static StatBase[] func_25153_a(String var0, int var1) {
		StatBase[] var2 = new StatBase[256];

		for(int var3 = 0; var3 < 256; ++var3) {
			if(Block.blocksList[var3] != null && Block.blocksList[var3].func_27033_k()) {
				String var4 = StatCollector.translateToLocalFormatted(var0, new Object[]{Block.blocksList[var3].func_25016_i()});
				var2[var3] = (new StatCrafting(var1 + var3, var4, var3)).func_25068_c();
				field_25185_d.add((StatCrafting)var2[var3]);
			}
		}

		replaceAllSimilarBlocks(var2);
		return var2;
	}

	private static StatBase[] func_25155_a(StatBase[] var0, String var1, int var2, int var3, int var4) {
		if(var0 == null) {
			var0 = new StatBase[32000];
		}

		for(int var5 = var3; var5 < var4; ++var5) {
			if(Item.itemsList[var5] != null) {
				String var6 = StatCollector.translateToLocalFormatted(var1, new Object[]{Item.itemsList[var5].func_25009_k()});
				var0[var5] = (new StatCrafting(var2 + var5, var6, var5)).func_25068_c();
				if(var5 >= Block.blocksList.length) {
					field_25186_c.add((StatCrafting)var0[var5]);
				}
			}
		}

		replaceAllSimilarBlocks(var0);
		return var0;
	}

	private static StatBase[] func_25149_b(StatBase[] var0, String var1, int var2, int var3, int var4) {
		if(var0 == null) {
			var0 = new StatBase[32000];
		}

		for(int var5 = var3; var5 < var4; ++var5) {
			if(Item.itemsList[var5] != null && Item.itemsList[var5].func_25007_g()) {
				String var6 = StatCollector.translateToLocalFormatted(var1, new Object[]{Item.itemsList[var5].func_25009_k()});
				var0[var5] = (new StatCrafting(var2 + var5, var6, var5)).func_25068_c();
			}
		}

		replaceAllSimilarBlocks(var0);
		return var0;
	}

	private static void replaceAllSimilarBlocks(StatBase[] var0) {
		replaceSimilarBlocks(var0, Block.waterStill.blockID, Block.waterMoving.blockID);
		replaceSimilarBlocks(var0, Block.lavaStill.blockID, Block.lavaStill.blockID);
		replaceSimilarBlocks(var0, Block.pumpkinLantern.blockID, Block.pumpkin.blockID);
		replaceSimilarBlocks(var0, Block.stoneOvenActive.blockID, Block.stoneOvenIdle.blockID);
		replaceSimilarBlocks(var0, Block.oreRedstoneGlowing.blockID, Block.oreRedstone.blockID);
		replaceSimilarBlocks(var0, Block.redstoneRepeaterActive.blockID, Block.redstoneRepeaterIdle.blockID);
		replaceSimilarBlocks(var0, Block.torchRedstoneActive.blockID, Block.torchRedstoneIdle.blockID);
		replaceSimilarBlocks(var0, Block.mushroomRed.blockID, Block.mushroomBrown.blockID);
		replaceSimilarBlocks(var0, Block.stairDouble.blockID, Block.stairSingle.blockID);
		replaceSimilarBlocks(var0, Block.grass.blockID, Block.dirt.blockID);
		replaceSimilarBlocks(var0, Block.tilledField.blockID, Block.dirt.blockID);
	}

	private static void replaceSimilarBlocks(StatBase[] var0, int var1, int var2) {
		if(var0[var1] != null && var0[var2] == null) {
			var0[var2] = var0[var1];
		} else {
			field_25188_a.remove(var0[var1]);
			field_25185_d.remove(var0[var1]);
			field_25187_b.remove(var0[var1]);
			var0[var1] = var0[var2];
		}
	}

	public static StatBase func_27361_a(int var0) {
		return (StatBase)field_25169_C.get(Integer.valueOf(var0));
	}

	static {
		AchievementList.func_27374_a();
		field_25166_D = false;
		field_25164_E = false;
	}
}
