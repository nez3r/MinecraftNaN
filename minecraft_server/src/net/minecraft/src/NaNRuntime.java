package net.minecraft.src;

public final class NaNRuntime {
	private NaNRuntime() {
	}

	public static boolean isHeadless() {
		try {
			Class var0 = Class.forName("java.awt.GraphicsEnvironment");
			return ((Boolean)var0.getMethod("isHeadless", new Class[0]).invoke(null, new Object[0])).booleanValue();
		} catch (Exception var1) {
			return true;
		}
	}
}
