package tfc.tingedlights.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.LevelWithColoredLightSupport;

public class LightApi {
	public static void updateLight(Level level, Light light, BlockPos pos) {
		if (level instanceof LevelWithColoredLightSupport) {
			((LevelWithColoredLightSupport) level).updateLight(light, pos);
		}
	}
}
