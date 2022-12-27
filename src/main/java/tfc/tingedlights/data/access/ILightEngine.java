package tfc.tingedlights.data.access;

import tfc.tingedlights.data.LightManager;

public interface ILightEngine {
	LightManager getManager();
	void setManager(LightManager manager);
}
