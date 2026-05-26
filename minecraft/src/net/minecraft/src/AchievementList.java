package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

public class AchievementList {
	public static int field_27392_a;
	public static int field_27391_b;
	public static int field_27390_c;
	public static int field_27389_d;
	public static List field_27388_e = new ArrayList();
	public static Achievement field_25195_b = (new Achievement(0, "openInventory", 0, 0, Item.book, (Achievement)null)).func_27089_a().func_27091_c();
	public static Achievement field_25198_c = (new Achievement(1, "mineWood", 2, 1, Block.wood, field_25195_b)).func_27091_c();
	public static Achievement field_25197_d = (new Achievement(2, "buildWorkBench", 4, -1, Block.workbench, field_25198_c)).func_27091_c();
	public static Achievement field_27387_i = (new Achievement(3, "buildPickaxe", 4, 2, Item.pickaxeWood, field_25197_d)).func_27091_c();
	public static Achievement field_27386_j = (new Achievement(4, "buildFurnace", 3, 4, Block.stoneOvenActive, field_27387_i)).func_27091_c();
	public static Achievement field_27385_k = (new Achievement(5, "acquireIron", 1, 4, Item.ingotIron, field_27386_j)).func_27091_c();
	public static Achievement field_27384_l = (new Achievement(6, "buildHoe", 2, -3, Item.hoeWood, field_25197_d)).func_27091_c();
	public static Achievement field_27383_m = (new Achievement(7, "makeBread", -1, -3, Item.bread, field_27384_l)).func_27091_c();
	public static Achievement field_27382_n = (new Achievement(8, "bakeCake", 0, -5, Item.cake, field_27384_l)).func_27091_c();
	public static Achievement field_27381_o = (new Achievement(9, "buildBetterPickaxe", 6, 2, Item.pickaxeStone, field_27387_i)).func_27091_c();
	public static Achievement field_27380_p = (new Achievement(10, "cookFish", 2, 6, Item.fishCooked, field_27386_j)).func_27091_c();
	public static Achievement field_27379_q = (new Achievement(11, "onARail", 2, 3, Block.minecartTrack, field_27385_k)).func_27094_b().func_27091_c();
	public static Achievement field_27378_r = (new Achievement(12, "buildSword", 6, -1, Item.swordWood, field_25197_d)).func_27091_c();
	public static Achievement field_27377_s = (new Achievement(13, "killEnemy", 8, -1, Item.bone, field_27378_r)).func_27091_c();
	public static Achievement field_27376_t = (new Achievement(14, "killCow", 7, -3, Item.leather, field_27378_r)).func_27091_c();
	public static Achievement field_27375_u = (new Achievement(15, "flyPig", 8, -4, Item.saddle, field_27376_t)).func_27094_b().func_27091_c();

	public static void func_27374_a() {
	}

	static {
		System.out.println(field_27388_e.size() + " achievements");
	}
}
