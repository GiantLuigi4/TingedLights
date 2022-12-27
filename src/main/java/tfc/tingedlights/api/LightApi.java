package tfc.tingedlights.api;

import net.minecraft.world.level.Level;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.LevelWithColoredLightSupport;

public class LightApi {
	public static void addLight(Level level, Light light) {
		if (level instanceof LevelWithColoredLightSupport) {
			((LevelWithColoredLightSupport) level).addLight(light);
		}
	}
	
	public static void removeLight(Level level, Light light) {
		if (level instanceof LevelWithColoredLightSupport) {
			((LevelWithColoredLightSupport) level).removeLight(light);
		}
	}
}
