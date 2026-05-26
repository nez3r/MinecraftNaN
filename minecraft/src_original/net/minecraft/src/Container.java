package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Container {
	public List field_20123_d = new ArrayList();
	public List slots = new ArrayList();
	public int windowId = 0;
	private short field_20917_a = 0;
	protected List field_20121_g = new ArrayList();
	private Set field_20918_b = new HashSet();

	protected void addSlot(Slot var1) {
		var1.slotNumber = this.slots.size();
		this.slots.add(var1);
		this.field_20123_d.add((Object)null);
	}

	public void updateCraftingResults() {
		for(int var1 = 0; var1 < this.slots.size(); ++var1) {
			ItemStack var2 = ((Slot)this.slots.get(var1)).getStack();
			ItemStack var3 = (ItemStack)this.field_20123_d.get(var1);
			if(!ItemStack.areItemStacksEqual(var3, var2)) {
				var3 = var2 == null ? null : var2.copy();
				this.field_20123_d.set(var1, var3);

				for(int var4 = 0; var4 < this.field_20121_g.size(); ++var4) {
					((ICrafting)this.field_20121_g.get(var4)).func_20159_a(this, var1, var3);
				}
			}
		}

	}

	public Slot getSlot(int var1) {
		return (Slot)this.slots.get(var1);
	}

	public ItemStack func_27279_a(int var1) {
		Slot var2 = (Slot)this.slots.get(var1);
		return var2 != null ? var2.getStack() : null;
	}

	public ItemStack func_27280_a(int var1, int var2, boolean var3, EntityPlayer var4) {
		ItemStack var5 = null;
		if(var2 == 0 || var2 == 1) {
			InventoryPlayer var6 = var4.inventory;
			if(var1 == -999) {
				if(var6.getItemStack() != null && var1 == -999) {
					if(var2 == 0) {
						var4.dropPlayerItem(var6.getItemStack());
						var6.setItemStack((ItemStack)null);
					}

					if(var2 == 1) {
						var4.dropPlayerItem(var6.getItemStack().splitStack(1));
						if(var6.getItemStack().stackSize == 0) {
							var6.setItemStack((ItemStack)null);
						}
					}
				}
			} else if(var3) {
				ItemStack var7 = this.func_27279_a(var1);
				if(var7 != null) {
					var5 = var7.copy();
				}
			} else {
				Slot var12 = (Slot)this.slots.get(var1);
				if(var12 != null) {
					var12.onSlotChanged();
					ItemStack var8 = var12.getStack();
					ItemStack var9 = var6.getItemStack();
					if(var8 != null) {
						var5 = var8.copy();
					}

					int var10;
					if(var8 == null) {
						if(var9 != null && var12.isItemValid(var9)) {
							var10 = var2 == 0 ? var9.stackSize : 1;
							if(var10 > var12.getSlotStackLimit()) {
								var10 = var12.getSlotStackLimit();
							}

							var12.putStack(var9.splitStack(var10));
							if(var9.stackSize == 0) {
								var6.setItemStack((ItemStack)null);
							}
						}
					} else {
						ItemStack var11;
						if(var9 == null) {
							var10 = var2 == 0 ? var8.stackSize : (var8.stackSize + 1) / 2;
							var11 = var12.decrStackSize(var10);
							if(var11 != null && var12.func_25014_f()) {
								var4.addStat(StatList.field_25158_z[var11.itemID], var11.stackSize);
							}

							var6.setItemStack(var11);
							if(var8.stackSize == 0) {
								var12.putStack((ItemStack)null);
							}

							var12.onPickupFromSlot(var6.getItemStack());
						} else if(var12.isItemValid(var9)) {
							if(var8.itemID != var9.itemID || var8.getHasSubtypes() && var8.getItemDamage() != var9.getItemDamage()) {
								if(var9.stackSize <= var12.getSlotStackLimit()) {
									var12.putStack(var9);
									var6.setItemStack(var8);
								}
							} else {
								var10 = var2 == 0 ? var9.stackSize : 1;
								if(var10 > var12.getSlotStackLimit() - var8.stackSize) {
									var10 = var12.getSlotStackLimit() - var8.stackSize;
								}

								if(var10 > var9.getMaxStackSize() - var8.stackSize) {
									var10 = var9.getMaxStackSize() - var8.stackSize;
								}

								var9.splitStack(var10);
								if(var9.stackSize == 0) {
									var6.setItemStack((ItemStack)null);
								}

								var8.stackSize += var10;
							}
						} else if(var8.itemID == var9.itemID && var9.getMaxStackSize() > 1 && (!var8.getHasSubtypes() || var8.getItemDamage() == var9.getItemDamage())) {
							var10 = var8.stackSize;
							if(var10 > 0 && var10 + var9.stackSize <= var9.getMaxStackSize()) {
								var9.stackSize += var10;
								var11 = var8.splitStack(var10);
								if(var11 != null && var12.func_25014_f()) {
									var4.addStat(StatList.field_25158_z[var11.itemID], var11.stackSize);
								}

								if(var8.stackSize == 0) {
									var12.putStack((ItemStack)null);
								}

								var12.onPickupFromSlot(var6.getItemStack());
							}
						}
					}
				}
			}
		}

		return var5;
	}

	public void onCraftGuiClosed(EntityPlayer var1) {
		InventoryPlayer var2 = var1.inventory;
		if(var2.getItemStack() != null) {
			var1.dropPlayerItem(var2.getItemStack());
			var2.setItemStack((ItemStack)null);
		}

	}

	public void onCraftMatrixChanged(IInventory var1) {
		this.updateCraftingResults();
	}

	public void putStackInSlot(int var1, ItemStack var2) {
		this.getSlot(var1).putStack(var2);
	}

	public void putStacksInSlots(ItemStack[] var1) {
		for(int var2 = 0; var2 < var1.length; ++var2) {
			this.getSlot(var2).putStack(var1[var2]);
		}

	}

	public void func_20112_a(int var1, int var2) {
	}

	public short func_20111_a(InventoryPlayer var1) {
		++this.field_20917_a;
		return this.field_20917_a;
	}

	public void func_20113_a(short var1) {
	}

	public void func_20110_b(short var1) {
	}

	public abstract boolean isUsableByPlayer(EntityPlayer var1);
}
