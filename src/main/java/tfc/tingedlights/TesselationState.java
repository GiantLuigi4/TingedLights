package tfc.tingedlights;

import tfc.tingedlights.data.Color;

public class TesselationState {
	public static ThreadLocal<Boolean> guiLighting = ThreadLocal.withInitial(() -> false);
	private static final Color GUILight = new Color(1, 1, 1);
	
	public static Color getDefault(Color defaultV) {
		if (guiLighting.get()) return GUILight;
		return defaultV;
	}
}
