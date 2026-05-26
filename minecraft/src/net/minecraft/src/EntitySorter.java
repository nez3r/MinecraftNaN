package net.minecraft.src;

import java.util.Comparator;

public class EntitySorter implements Comparator {
	private Entity entityForSorting;

	public EntitySorter(Entity var1) {
		this.entityForSorting = var1;
	}

	public int sortByDistanceToEntity(WorldRenderer var1, WorldRenderer var2) {
		return var1.distanceToEntitySquared(this.entityForSorting) < var2.distanceToEntitySquared(this.entityForSorting) ? -1 : 1;
	}

	public int compare(Object var1, Object var2) {
		return this.sortByDistanceToEntity((WorldRenderer)var1, (WorldRenderer)var2);
	}
}
