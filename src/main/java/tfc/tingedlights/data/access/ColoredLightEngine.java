package tfc.tingedlights.data.access;

import tfc.tingedlights.data.LightManager;

public interface ColoredLightEngine extends LightManager, ILightEngine {
	@Override
	default LightManager getManager() {
		return this;
	}
}
