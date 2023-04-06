package tfc.tingedlights.data.access;

import tfc.tingedlights.data.LightManager;

public interface LevelWithColoredLightSupport extends ColoredLightEngine {
	LightManager getManager();
}
