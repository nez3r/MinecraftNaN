package net.minecraft.src;

public class SlotFurnace extends Slot {
	private EntityPlayer field_27011_d;

	public SlotFurnace(EntityPlayer var1, IInventory var2, int var3, int var4, int var5) {
		super(var2, var3, var4, var5);
		this.field_27011_d = var1;
	}

	public boolean isItemValid(ItemStack var1) {
		return false;
	}

	public boolean func_25014_f() {
		return true;
	}

	public void onPickupFromSlot(ItemStack var1) {
		if(var1.itemID == Item.ingotIron.shiftedIndex) {
			this.field_27011_d.addStat(AchievementList.field_27385_k, 1);
		}

		if(var1.itemID == Item.fishCooked.shiftedIndex) {
			this.field_27011_d.addStat(AchievementList.field_27380_p, 1);
		}

		super.onPickupFromSlot(var1);
	}
}
